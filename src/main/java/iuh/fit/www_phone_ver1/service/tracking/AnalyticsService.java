package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
  private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final UserEventRepository events;
  private final MongoTemplate mongo;
  private final OrderRepository orders;
  private final ProductRepository products;
  public AnalyticsService(
      UserEventRepository events,
      MongoTemplate mongo,
      OrderRepository orders,
      ProductRepository products) {
    this.events = events;
    this.mongo = mongo;
    this.orders = orders;
    this.products = products;
  }

  public record Report(
      long views,
      double conversionRate,
      long orderCount,
      long newVisitors,
      long chatbotMessages,
      List<DailyView> dailyViews,
      List<TopProduct> topProducts,
      List<TopKeyword> topKeywords) {}

  public record TopProduct(
      Long productId, String productName, long views, long clicks, long cartAdds, double conversionRate) {}

  public record TopKeyword(String keyword, long count) {}

  public record DailyView(LocalDate date, long count) {}

  public Report report(LocalDate fromDate, LocalDate toDate) {
    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);
    long views = events.countByEventTypeAndCreatedAtBetween("PAGE_VIEW", from, to);
    long clicks = events.countByEventTypeAndCreatedAtBetween("PRODUCT_CLICK", from, to);
    long cartAdds = events.countByEventTypeAndCreatedAtBetween("ADD_TO_CART", from, to);
    long chatbotMessages = events.countByEventTypeAndCreatedAtBetween("CHAT_MESSAGE", from, to);
    double conversionRate = clicks == 0 ? 0 : (double) cartAdds * 100 / clicks;

    return new Report(
        views,
        conversionRate,
        orders.countByOrderDateBetween(from, to),
        countNewVisitors(from, to),
        chatbotMessages,
        dailyViews(fromDate, toDate),
        topProducts(from, to),
        topKeywords(from, to));
  }

  private long countNewVisitors(LocalDateTime from, LocalDateTime to) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("eventType")
                    .is("PAGE_VIEW")
                    .and("sessionId")
                    .nin(null, "")),
            Aggregation.group("sessionId").min("createdAt").as("firstSeen"),
            Aggregation.match(Criteria.where("firstSeen").gte(from).lte(to)),
            Aggregation.count().as("count"));
    AggregationResults<CountRow> result =
        mongo.aggregate(aggregation, "user_events", CountRow.class);
    CountRow count = result.getUniqueMappedResult();
    return count == null ? 0 : count.getCount();
  }

  private List<DailyView> dailyViews(LocalDate fromDate, LocalDate toDate) {
    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("eventType").is("PAGE_VIEW").and("createdAt").gte(from).lte(to)),
            Aggregation.project()
                .and(
                    DateOperators.DateToString.dateOf("createdAt")
                        .toString("%Y-%m-%d")
                        .withTimezone(DateOperators.Timezone.fromZone(BUSINESS_TIME_ZONE)))
                .as("day"),
            Aggregation.group("day").count().as("count"),
            Aggregation.sort(Sort.Direction.ASC, "_id"));
    AggregationResults<DailyViewRow> result =
        mongo.aggregate(aggregation, "user_events", DailyViewRow.class);
    Map<LocalDate, Long> counts =
        result.getMappedResults().stream()
            .collect(
                Collectors.toMap(
                    row -> LocalDate.parse(row.getId()), DailyViewRow::getCount, Long::sum));

    List<DailyView> rows = new ArrayList<>();
    for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1)) {
      rows.add(new DailyView(day, counts.getOrDefault(day, 0L)));
    }
    return rows;
  }

  private List<TopProduct> topProducts(LocalDateTime from, LocalDateTime to) {
    var pageViews =
        ConditionalOperators.when(Criteria.where("eventType").is("PAGE_VIEW"))
            .then(1)
            .otherwise(0);
    var productClicks =
        ConditionalOperators.when(Criteria.where("eventType").is("PRODUCT_CLICK"))
            .then(1)
            .otherwise(0);
    var addToCarts =
        ConditionalOperators.when(Criteria.where("eventType").is("ADD_TO_CART"))
            .then(1)
            .otherwise(0);
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("eventType")
                    .in("PAGE_VIEW", "PRODUCT_CLICK", "ADD_TO_CART")
                    .and("productId")
                    .ne(null)
                    .and("createdAt")
                    .gte(from)
                    .lte(to)),
            Aggregation.group("productId")
                .sum(pageViews)
                .as("views")
                .sum(productClicks)
                .as("clicks")
                .sum(addToCarts)
                .as("cartAdds"),
            Aggregation.sort(Sort.Direction.DESC, "views"),
            Aggregation.limit(10));

    AggregationResults<TopProductRow> result =
        mongo.aggregate(aggregation, "user_events", TopProductRow.class);
    List<TopProductRow> rows = result.getMappedResults();
    List<Long> productIds = rows.stream().map(TopProductRow::getId).toList();
    Map<Long, Product> productById =
        products.findAllById(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

    return rows.stream()
        .map(
            row -> {
              long clicks = row.getClicks();
              double conversionRate = clicks == 0 ? 0 : (double) row.getCartAdds() * 100 / clicks;
              Product product = productById.get(row.getId());
              String name = product == null ? "Sản phẩm #" + row.getId() : product.getName();
              return new TopProduct(
                  row.getId(), name, row.getViews(), clicks, row.getCartAdds(), conversionRate);
            })
        .toList();
  }

  private List<TopKeyword> topKeywords(LocalDateTime from, LocalDateTime to) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("eventType")
                    .is("SEARCH")
                    .and("keyword")
                    .nin(null, "")
                    .and("createdAt")
                    .gte(from)
                    .lte(to)),
            Aggregation.group("keyword").count().as("count"),
            Aggregation.sort(Sort.Direction.DESC, "count"),
            Aggregation.limit(10));
    AggregationResults<TopKeywordRow> result =
        mongo.aggregate(aggregation, "user_events", TopKeywordRow.class);
    return result.getMappedResults().stream()
        .map(row -> new TopKeyword(row.getId(), row.getCount()))
        .toList();
  }

  public static class DailyViewRow {
    private String id;
    private long count;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
  }

  public static class TopProductRow {
    private Long id;
    private long views;
    private long clicks;
    private long cartAdds;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }
    public long getClicks() { return clicks; }
    public void setClicks(long clicks) { this.clicks = clicks; }
    public long getCartAdds() { return cartAdds; }
    public void setCartAdds(long cartAdds) { this.cartAdds = cartAdds; }
  }

  public static class TopKeywordRow {
    private String id;
    private long count;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
  }

  public static class CountRow {
    private long count;
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
  }
}
