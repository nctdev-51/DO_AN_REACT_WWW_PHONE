package iuh.fit.www_phone_ver1.service.tracking;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

class AnalyticsServiceTests {
  @Test
  void topKeywordsBuildsMongoCriteriaWithoutDuplicateKeywordKeys() {
    MongoTemplate mongo = mock(MongoTemplate.class);
    AggregationResults<AnalyticsService.TopKeywordRow> emptyResults =
        new AggregationResults<>(List.of(), new Document());
    when(mongo.aggregate(
            any(Aggregation.class),
            eq("user_events"),
            eq(AnalyticsService.TopKeywordRow.class)))
        .thenReturn(emptyResults);
    AnalyticsService service = new AnalyticsService(mock(UserEventRepository.class), mongo);

    assertTrue(service.topKeywords(LocalDateTime.now().minusDays(30), LocalDateTime.now()).isEmpty());
  }
}
