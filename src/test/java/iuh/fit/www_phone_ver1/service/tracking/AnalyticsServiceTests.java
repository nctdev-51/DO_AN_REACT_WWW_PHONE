package iuh.fit.www_phone_ver1.service.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductVariant;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.Interaction;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.KeywordStats;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.ProductStats;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

class AnalyticsServiceTests {
  @Test
  void flagsFivefoldViewSpikeWithTenUnitsAndNoCartAdds() {
    MongoTemplate mongo = mock(MongoTemplate.class);
    ProductRepository products = mock(ProductRepository.class);
    OrderRepository orders = mock(OrderRepository.class);
    ProductVariant variant = new ProductVariant();
    variant.setActive(true);
    variant.setStock(10);
    when(products.findAll()).thenReturn(List.of(
        Product.builder().id(2L).name("Galaxy").variants(List.of(variant)).build()));
    var recent = new AnalyticsService.TopProductRow();
    recent.setId(2L);
    recent.setViews(50);
    var baseline = new AnalyticsService.TopProductRow();
    baseline.setId(2L);
    baseline.setViews(70);
    when(mongo.aggregate(any(Aggregation.class), eq("user_events"),
        eq(AnalyticsService.TopProductRow.class)))
        .thenReturn(new AggregationResults<>(List.of(recent), new Document()),
            new AggregationResults<>(List.of(baseline), new Document()));

    var result = new AnalyticsService(mock(UserEventRepository.class), mongo, orders, products,
        mock(DailyTrackingStats.class), null)
        .forecastStockoutRisk();

    assertEquals(List.of("HIGH", "MODERATE"),
        result.stream().map(AnalyticsService.StockoutForecast::riskLevel).toList());
    assertTrue(result.get(0).riskBadge().contains("48 giờ"));
  }

  @Test
  void reportReadsDailySummariesAndFiltersProductInteractionsByCategory() {
    MongoTemplate mongo = mock(MongoTemplate.class);
    UserEventRepository events = mock(UserEventRepository.class);
    OrderRepository orders = mock(OrderRepository.class);
    ProductRepository products = mock(ProductRepository.class);
    DailyTrackingStats dailyStats = mock(DailyTrackingStats.class);
    AnalyticsService.CountRow countRow = new AnalyticsService.CountRow();
    countRow.setCount(8);
    when(mongo.aggregate(
            any(Aggregation.class),
            eq("user_events"),
            eq(AnalyticsService.CountRow.class)))
        .thenReturn(new AggregationResults<>(List.of(countRow), new Document()));
    when(events.countByEventTypeAndCreatedAtBetween(
            eq("CHAT_MESSAGE"), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(3L);
    when(orders.countByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(5L);
    when(products.findAllById(any())).thenReturn(List.of());

    AnalyticsService service = new AnalyticsService(events, mongo, orders, products, dailyStats, null);
    LocalDate from = LocalDate.of(2026, 11, 1);
    LocalDate to = LocalDate.of(2026, 11, 7);
    when(dailyStats.read(from, to)).thenReturn(List.of(
        new UserEventDailyStats(from.toString(), Instant.EPOCH, 1, new Interaction(12, 4, 2),
            List.of(new ProductStats(46L, 7L, new Interaction(2, 1, 1)),
                new ProductStats(47L, 8L, new Interaction(10, 3, 1))),
            List.of(new KeywordStats("iphone", 2)))));
    var report = service.report(from, to, 7L);

    assertEquals(12, report.views());
    assertEquals(50, report.conversionRate());
    assertEquals(5, report.orderCount());
    assertEquals(8, report.newVisitors());
    assertEquals(3, report.chatbotMessages());
    assertEquals(7, report.dailyViews().size());
    assertEquals(from, report.dailyViews().get(0).date());
    assertEquals(12, report.dailyViews().get(0).count());
    assertEquals(to, report.dailyViews().get(report.dailyViews().size() - 1).date());
    assertEquals(List.of(46L), report.topProducts().stream()
        .map(AnalyticsService.TopProduct::productId).toList());
    assertEquals(List.of(new AnalyticsService.TopKeyword("iphone", 2)), report.topKeywords());
  }
}
