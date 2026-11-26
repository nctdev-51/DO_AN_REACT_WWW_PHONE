package iuh.fit.www_phone_ver1.service.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

class AnalyticsServiceTests {
  @Test
  void reportBuildsMongoQueriesAndUsesTheSelectedRange() {
    MongoTemplate mongo = mock(MongoTemplate.class);
    UserEventRepository events = mock(UserEventRepository.class);
    OrderRepository orders = mock(OrderRepository.class);
    ProductRepository products = mock(ProductRepository.class);
    when(mongo.aggregate(
            any(Aggregation.class),
            eq("user_events"),
            eq(AnalyticsService.DailyViewRow.class)))
        .thenReturn(new AggregationResults<>(List.of(), new Document()));
    when(mongo.aggregate(
            any(Aggregation.class),
            eq("user_events"),
            eq(AnalyticsService.TopProductRow.class)))
        .thenReturn(new AggregationResults<>(List.of(), new Document()));
    when(mongo.aggregate(
            any(Aggregation.class),
            eq("user_events"),
            eq(AnalyticsService.TopKeywordRow.class)))
        .thenReturn(new AggregationResults<>(List.of(), new Document()));
    AnalyticsService.CountRow countRow = new AnalyticsService.CountRow();
    countRow.setCount(8);
    when(mongo.aggregate(
            any(Aggregation.class),
            eq("user_events"),
            eq(AnalyticsService.CountRow.class)))
        .thenReturn(new AggregationResults<>(List.of(countRow), new Document()));
    when(events.countByEventTypeAndCreatedAtBetween(
            eq("PAGE_VIEW"), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(12L);
    when(events.countByEventTypeAndCreatedAtBetween(
            eq("PRODUCT_CLICK"), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(4L);
    when(events.countByEventTypeAndCreatedAtBetween(
            eq("ADD_TO_CART"), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(2L);
    when(events.countByEventTypeAndCreatedAtBetween(
            eq("CHAT_MESSAGE"), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(3L);
    when(orders.countByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(5L);
    when(products.findAllById(any())).thenReturn(List.of());

    AnalyticsService service = new AnalyticsService(events, mongo, orders, products);
    LocalDate from = LocalDate.of(2026, 11, 1);
    LocalDate to = LocalDate.of(2026, 11, 7);
    var report = service.report(from, to);

    assertEquals(12, report.views());
    assertEquals(50, report.conversionRate());
    assertEquals(5, report.orderCount());
    assertEquals(8, report.newVisitors());
    assertEquals(3, report.chatbotMessages());
    assertEquals(7, report.dailyViews().size());
    assertEquals(from, report.dailyViews().get(0).date());
    assertEquals(to, report.dailyViews().get(report.dailyViews().size() - 1).date());
  }
}
