package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
  private final UserEventRepository events;
  private final MongoTemplate mongo;

  public AnalyticsService(UserEventRepository events, MongoTemplate mongo) {
    this.events = events;
    this.mongo = mongo;
  }

  public record Summary(
      long views7Days,
      long views30Days,
      double conversionRate,
      long chatbotMessages,
      List<TopProduct> topProducts,
      List<TopKeyword> topKeywords) {}

  public record TopProduct(Long productId, long views, long cartAdds) {}

  public record TopKeyword(String keyword, long count) {}

  public record DailyView(LocalDate date, long count) {}

  public Summary summary() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime sevenDays = now.minusDays(7);
    LocalDateTime thirtyDays = now.minusDays(30);
    long views7 = events.countByEventTypeAndCreatedAtBetween("PAGE_VIEW", sevenDays, now);
    long views30 = events.countByEventTypeAndCreatedAtBetween("PAGE_VIEW", thirtyDays, now);
    long clicks = events.countByEventTypeAndCreatedAtBetween("PRODUCT_CLICK", thirtyDays, now);
    long cartAdds = events.countByEventTypeAndCreatedAtBetween("ADD_TO_CART", thirtyDays, now);
    long chat = events.countByEventTypeAndCreatedAtBetween("CHAT_MESSAGE", thirtyDays, now);
    double conversion = clicks == 0 ? 0 : (double) cartAdds * 100 / clicks;
    return new Summary(
        views7,
        views30,
        conversion,
        chat,
        topProducts(thirtyDays, now),
        topKeywords(thirtyDays, now));
  }

  public List<DailyView> dailyViews(int days) {
    LocalDateTime from = LocalDate.now().minusDays(days - 1).atStartOfDay();
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("eventType")
                    .is("PAGE_VIEW")
                    .and("createdAt")
                    .gte(from)
                    .lte(LocalDateTime.now())),
            Aggregation.project()
                .andExpression("dateToString('%Y-%m-%d', createdAt)").as("day"),
            Aggregation.group("day").count().as("count"),
            Aggregation.sort(Sort.Direction.ASC, "_id"));
    AggregationResults<DailyViewRow> result =
        mongo.aggregate(aggregation, "user_events", DailyViewRow.class);

    List<DailyView> rows = new ArrayList<>();
    for (DailyViewRow row : result) {
      rows.add(new DailyView(LocalDate.parse(row.getId()), row.getCount()));
    }
    return rows;
  }

  public List<TopProduct> topProducts(LocalDateTime from, LocalDateTime to) {
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
                .count().as("allEvents"),
            Aggregation.sort(Sort.Direction.DESC, "allEvents"),
            Aggregation.limit(10));

    AggregationResults<TopProductRow> result =
        mongo.aggregate(aggregation, "user_events", TopProductRow.class);
    List<TopProduct> products = new ArrayList<>();
    for (TopProductRow row : result) {
      products.add(new TopProduct(row.getId(), row.getAllEvents(), row.getCartAdds()));
    }
    return products;
  }

  public List<TopKeyword> topKeywords(LocalDateTime from, LocalDateTime to) {
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
    List<TopKeyword> keywords = new ArrayList<>();
    for (TopKeywordRow row : result) {
      keywords.add(new TopKeyword(row.getId(), row.getCount()));
    }
    return keywords;
  }

  public long countNewUsersInLast(LocalDateTime from) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("eventType")
                    .is("PAGE_VIEW")
                    .and("createdAt")
                    .gte(from)),
            Aggregation.group("sessionId").first("createdAt").as("firstSeen"),
            Aggregation.match(Criteria.where("firstSeen").gte(from)),
            Aggregation.count().as("count"));
    AggregationResults<CountRow> result =
        mongo.aggregate(aggregation, "user_events", CountRow.class);
    CountRow first = result.getUniqueMappedResult();
    return first == null ? 0 : first.getCount();
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
    private long allEvents;
    private long cartAdds;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getAllEvents() { return allEvents; }
    public void setAllEvents(long allEvents) { this.allEvents = allEvents; }
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
