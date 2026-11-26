package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.Interaction;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

  private final UserEventRepository events;
  private final MongoTemplate mongo;
  private final OrderRepository orders;
  private final ProductRepository products;
  private final JdbcTemplate jdbcTemplate;
  private final DailyTrackingStats dailyStats;

  @Autowired
  public AnalyticsService(
      UserEventRepository events,
      MongoTemplate mongo,
      OrderRepository orders,
      ProductRepository products,
      DailyTrackingStats dailyStats,
      ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
    this.events = events;
    this.mongo = mongo;
    this.orders = orders;
    this.products = products;
    this.dailyStats = dailyStats;
    this.jdbcTemplate = jdbcTemplateProvider == null ? null : jdbcTemplateProvider.getIfAvailable();
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

  // Chức năng 5: DTO Dự báo nguy cơ cháy hàng
  public record StockoutForecast(
      Long productId,
      String productName,
      int stock,
      long views,
      long cartAdds,
      String riskLevel,
      String riskBadge,
      String recommendation) {}

  // Chức năng 6: DTO Phân tích câu hỏi Chatbot AI
  public record AiChatInsight(String question, String topicTag, String timeAgo) {}

  public record AiTopicDistribution(String topic, long count, int percentage) {}


  public Report report(LocalDate fromDate, LocalDate toDate) {
    return report(fromDate, toDate, null);
  }

  public Report report(LocalDate fromDate, LocalDate toDate, Long categoryId) {
    var summaries = dailyStats.read(fromDate, toDate);
    Interaction totals = summaries.stream().map(UserEventDailyStats::totals)
        .reduce(Interaction.ZERO, Interaction::plus);
    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);
    long chatbotMessages = events.countByEventTypeAndCreatedAtBetween("CHAT_MESSAGE", from, to);
    double conversionRate = totals.clicks() == 0 ? 0 : (double) totals.cartAdds() * 100 / totals.clicks();

    return new Report(
        totals.views(),
        conversionRate,
        orders.countByOrderDateBetween(from, to),
        countNewVisitors(from, to),
        chatbotMessages,
        dailyViews(fromDate, toDate, summaries),
        topProducts(summaries, categoryId),
        topKeywords(summaries));
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

  private List<DailyView> dailyViews(
      LocalDate from, LocalDate to, List<UserEventDailyStats> summaries) {
    Map<LocalDate, Long> counts = summaries.stream().collect(Collectors.toMap(
        row -> LocalDate.parse(row.day()), row -> row.totals().views()));
    return from.datesUntil(to.plusDays(1))
        .map(day -> new DailyView(day, counts.getOrDefault(day, 0L))).toList();
  }

  private List<TopProduct> topProducts(List<UserEventDailyStats> summaries, Long categoryId) {
    Map<Long, Interaction> counts = new HashMap<>();
    summaries.stream().flatMap(day -> day.products().stream())
        .filter(row -> categoryId == null || categoryId.equals(row.categoryId()))
        .forEach(row -> counts.merge(row.productId(), row.interaction(), Interaction::plus));

    var ranked = counts.entrySet().stream()
        .sorted(Map.Entry.<Long, Interaction>comparingByValue(
            Comparator.comparingLong(Interaction::views).reversed())
            .thenComparing(Map.Entry.comparingByKey()))
        .limit(10).toList();
    var names = products.findAllById(ranked.stream().map(Map.Entry::getKey).toList())
        .stream().collect(Collectors.toMap(Product::getId, Product::getName));
    return ranked.stream().map(entry -> {
      long productId = entry.getKey();
      Interaction row = entry.getValue();
      double rate = row.clicks() == 0 ? 0 : (double) row.cartAdds() * 100 / row.clicks();
      return new TopProduct(productId, names.getOrDefault(productId, "Sản phẩm #" + productId),
          row.views(), row.clicks(), row.cartAdds(), rate);
    }).toList();
  }

  private List<TopKeyword> topKeywords(List<UserEventDailyStats> summaries) {
    Map<String, Long> counts = new HashMap<>();
    summaries.stream().flatMap(day -> day.keywords().stream())
        .forEach(row -> counts.merge(row.keyword(), row.count(), Long::sum));
    return counts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .limit(10)
        .map(entry -> new TopKeyword(entry.getKey(), entry.getValue())).toList();
  }

  // ─── CHỨC NĂNG 5: DỰ BÁO NHU CẦU & CẢNH BÁO NGUY CƠ CHÁY HÀNG ───────────────
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public List<StockoutForecast> forecastStockoutRisk() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime recent = now.minusDays(2);
    LocalDateTime baseline = recent.minusDays(14);
    var views = ConditionalOperators.when(Criteria.where("eventType").is("PAGE_VIEW"))
        .then(1).otherwise(0);
    var cartAdds = ConditionalOperators.when(Criteria.where("eventType").is("ADD_TO_CART"))
        .then(1).otherwise(0);
    Map<Long, TopProductRow> current = stockInteractions(recent, now, views, cartAdds);
    Map<Long, TopProductRow> previous = stockInteractions(baseline, recent, views, cartAdds);
    var cancelled = iuh.fit.www_phone_ver1.service.order.OrderStatuses.CANCELLED;
    var recentOrders = orders.productDemand(recent, now, cancelled).stream()
        .collect(Collectors.toMap(OrderRepository.ProductDemand::getProductId,
            OrderRepository.ProductDemand::getUnits));
    var previousOrders = orders.productDemand(baseline, recent, cancelled).stream()
        .collect(Collectors.toMap(OrderRepository.ProductDemand::getProductId,
            OrderRepository.ProductDemand::getUnits));

    List<StockoutForecast> forecasts = new ArrayList<>();
    for (Product product : products.findAll()) {
      if (!Integer.valueOf(1).equals(product.getStatus())) continue;
      Long id = product.getId();
      int stock = product.getStock();
      TopProductRow row = current.getOrDefault(id, new TopProductRow());
      long recentViews = row.getViews();
      long carts = row.getCartAdds();
      double baselineViews = previous.getOrDefault(id, new TopProductRow()).getViews() / 7.0;
      boolean spike = baselineViews >= 1 && recentViews >= 20 && recentViews >= 5 * baselineViews;
      double expectedUnits = 0.7 * recentOrders.getOrDefault(id, 0L)
          + 0.3 * previousOrders.getOrDefault(id, 0L) / 7.0;
      if (stock == 0) {
        forecasts.add(new StockoutForecast(id, product.getName(), stock, recentViews, carts,
            "OUT", "Đã hết hàng", "Kiểm tra kế hoạch nhập bổ sung."));
      } else if (expectedUnits >= stock || (stock <= 10 && spike)) {
        String reason = expectedUnits >= stock
            ? String.format("Dự báo %.1f máy trong 48 giờ theo đơn hàng. ", expectedUnits)
            : String.format("Lượt xem tăng %.1f lần so với mức nền. ", recentViews / baselineViews);
        forecasts.add(new StockoutForecast(id, product.getName(), stock, recentViews, carts,
            "HIGH", "Nguy cơ hết hàng trong 48 giờ", reason + "Xem xét tạo đơn nhập hàng."));
      }
      if (stock > 0 && recentViews >= 20 && carts == 0) {
        forecasts.add(new StockoutForecast(id, product.getName(), stock, recentViews, carts,
            "MODERATE", "Nhiều lượt xem, chưa thêm giỏ",
            "Kiểm tra giá, mô tả sản phẩm và thao tác thêm giỏ."));
      }
    }
    forecasts.sort(Comparator.comparingInt((StockoutForecast f) ->
        "HIGH".equals(f.riskLevel()) ? 0 : "MODERATE".equals(f.riskLevel()) ? 1 : 2)
        .thenComparing(Comparator.comparingLong(StockoutForecast::views).reversed())
        .thenComparing(StockoutForecast::productId));
    return forecasts;
  }

  private Map<Long, TopProductRow> stockInteractions(
      LocalDateTime from, LocalDateTime to,
      ConditionalOperators.Cond views, ConditionalOperators.Cond cartAdds) {
    var aggregation = Aggregation.newAggregation(
        Aggregation.match(Criteria.where("eventType").in("PAGE_VIEW", "ADD_TO_CART")
            .and("productId").ne(null).and("createdAt").gte(from).lt(to)),
        Aggregation.group("productId").sum(views).as("views").sum(cartAdds).as("cartAdds"));
    return mongo.aggregate(aggregation, "user_events", TopProductRow.class).getMappedResults()
        .stream().collect(Collectors.toMap(TopProductRow::getId, Function.identity()));
  }

  public List<AiChatInsight> recentAiChatInsights(int limit) {
    if (jdbcTemplate != null) {
      try {
        return jdbcTemplate.query(
            "SELECT message_text, created_at FROM chat_memory_messages WHERE LOWER(message_type) = 'user' ORDER BY id DESC LIMIT ?",
            (rs, i) -> {
              String text = rs.getString("message_text");
              java.sql.Timestamp ts = rs.getTimestamp("created_at");
              String timeAgo = ts != null ? formatTimeAgo(ts.toLocalDateTime()) : "Gần đây";
              return new AiChatInsight(text, iuh.fit.www_phone_ver1.service.ai.ChatTopic.classify(text).label(), timeAgo);
            },
            limit);
      } catch (Exception ex) {
        // Fallback
      }
    }
    return List.of();
  }

  public List<AiTopicDistribution> aiTopicDistribution(LocalDate from, LocalDate to) {
    var aggregation = Aggregation.newAggregation(
        Aggregation.match(Criteria.where("eventType").is("CHAT_MESSAGE")
            .and("createdAt").gte(from.atStartOfDay()).lt(to.plusDays(1).atStartOfDay())),
        Aggregation.group("meta.topic").count().as("count"),
        Aggregation.sort(Sort.by(Sort.Order.desc("count"), Sort.Order.asc("_id"))));
    var rows = mongo.aggregate(aggregation, "user_events", TopKeywordRow.class).getMappedResults();
    long total = rows.stream().mapToLong(TopKeywordRow::getCount).sum();
    return rows.stream().map(row -> new AiTopicDistribution(
        row.getId() == null ? "Chưa phân loại" : row.getId(),
        row.getCount(), (int) Math.round(100.0 * row.getCount() / total))).toList();
  }

  private String formatTimeAgo(LocalDateTime dt) {
    if (dt == null) return "Gần đây";
    long mins = Duration.between(dt, LocalDateTime.now()).toMinutes();
    if (mins < 1) return "Vừa xong";
    if (mins < 60) return mins + " phút trước";
    long hours = mins / 60;
    if (hours < 24) return hours + " giờ trước";
    return (hours / 24) + " ngày trước";
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
