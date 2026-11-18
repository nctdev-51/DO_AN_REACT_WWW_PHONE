package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.service.vector.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduled batch job that reindexes active catalog products into MongoDB and Milvus. */
@Component
@ConditionalOnProperty(name = "app.recommendation.reindex-enabled", havingValue = "true")
public class VectorIndexJob {

  private static final Logger log = LoggerFactory.getLogger(VectorIndexJob.class);

  private final RecommendationService recommendationService;
  private final boolean indexOnStartup;

  public VectorIndexJob(
      RecommendationService recommendationService,
      @Value("${app.recommendation.index-on-startup:false}") boolean indexOnStartup) {
    this.recommendationService = recommendationService;
    this.indexOnStartup = indexOnStartup;
  }

  @Async
  @EventListener(ApplicationReadyEvent.class)
  public void warmUpOnStartup() {
    if (!indexOnStartup) {
      return;
    }
    try {
      RecommendationService.ReindexSummary summary = recommendationService.reindexAll();
      log.info(
          "Startup vector warmup finished: {}/{} products indexed (milvusAvailable={})",
          summary.indexed(),
          summary.totalProducts(),
          summary.milvusAvailable());
    } catch (Exception ex) {
      log.warn("Startup vector warmup skipped: {}", ex.getMessage());
    }
  }

  @Scheduled(cron = "${app.recommendation.reindex-cron:0 0 2 * * *}")
  public void reindexAll() {
    RecommendationService.ReindexSummary summary = recommendationService.reindexAll();
    log.info(
        "Scheduled vector reindex finished: total={}, indexed={}, failed={}",
        summary.totalProducts(),
        summary.indexed(),
        summary.failed());
  }
}
