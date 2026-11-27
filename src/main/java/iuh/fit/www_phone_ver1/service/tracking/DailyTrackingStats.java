package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.Interaction;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.KeywordStats;
import iuh.fit.www_phone_ver1.entity.mongo.UserEventDailyStats.ProductStats;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DailyTrackingStats {
  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
  private static final String EVENTS = "user_events";
  private static final int RULES_VERSION = 1;

  private final MongoTemplate mongo;
  private final ProductRepository products;
  private boolean historyLoaded;

  public DailyTrackingStats(MongoTemplate mongo, ProductRepository products) {
    this.mongo = mongo;
    this.products = products;
  }

  @Scheduled(initialDelayString = "${app.tracking.daily-stats.initial-delay-ms:5000}",
      fixedDelayString = "${app.tracking.daily-stats.interval-ms:60000}")
  public void refresh() {
    LocalDate today = LocalDate.now(BUSINESS_ZONE);
    LocalDate yesterday = today.minusDays(1);
    buildDay(today, false);
    buildDay(yesterday, false);

    if (!historyLoaded) {
      var oldestQuery = Query.query(Criteria.where("createdAt").type(9))
          .with(Sort.by("createdAt"));
      oldestQuery.fields().include("createdAt");
      Document oldest = mongo.findOne(oldestQuery, Document.class, EVENTS);
      if (oldest != null) {
        LocalDate first = oldest.getDate("createdAt").toInstant().atZone(BUSINESS_ZONE).toLocalDate();
        for (LocalDate day = first; day.isBefore(yesterday); day = day.plusDays(1)) {
          if (!mongo.exists(Query.query(Criteria.where("_id").is(day.toString())),
              UserEventDailyStats.class)) {
            buildDay(day, true);
          }
        }
      }
      historyLoaded = true;
    }
  }

  public List<UserEventDailyStats> read(LocalDate from, LocalDate to) {
    return mongo.find(
        Query.query(Criteria.where("_id").gte(from.toString()).lte(to.toString())),
        UserEventDailyStats.class);
  }

  private void buildDay(LocalDate day, boolean historical) {
    Instant started = Instant.now();
    Date from = Date.from(day.atStartOfDay(BUSINESS_ZONE).toInstant());
    Date to = Date.from(day.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant());
    var aggregation = Aggregation.newAggregation(
        Aggregation.match(Criteria.where("createdAt").gte(from).lt(to)
            .and("eventType").in("PAGE_VIEW", "PRODUCT_CLICK", "ADD_TO_CART", "SEARCH")),
        Aggregation.group("eventType", "productId", "keyword").count().as("count"));
    var rows = mongo.aggregate(aggregation, EVENTS, Document.class).getMappedResults();

    Interaction totals = Interaction.ZERO;
    Map<Long, Interaction> byProduct = new HashMap<>();
    Map<String, Long> byKeyword = new HashMap<>();
    for (Document row : rows) {
      Document key = row.get("_id", Document.class);
      long count = ((Number) row.get("count")).longValue();
      String type = key.getString("eventType");
      if ("SEARCH".equals(type)) {
        String raw = key.getString("keyword");
        if (raw != null && !raw.isBlank()) {
          byKeyword.merge(raw.strip().toLowerCase(Locale.ROOT), count, Long::sum);
        }
        continue;
      }
      Interaction interaction = switch (type) {
        case "PAGE_VIEW" -> new Interaction(count, 0, 0);
        case "PRODUCT_CLICK" -> new Interaction(0, count, 0);
        case "ADD_TO_CART" -> new Interaction(0, 0, count);
        default -> throw new IllegalStateException("Unexpected tracking event type: " + type);
      };
      totals = totals.plus(interaction);
      if (key.get("productId") instanceof Number productId) {
        byProduct.merge(productId.longValue(), interaction, Interaction::plus);
      }
    }
    if (historical && rows.isEmpty()) {
      return;
    }

    Map<Long, Long> categoryIds = new HashMap<>();
    if (!byProduct.isEmpty()) {
      products.findTrackingCategories(byProduct.keySet())
          .forEach(row -> categoryIds.put(row.getProductId(), row.getCategoryId()));
    }
    List<ProductStats> productStats = new ArrayList<>();
    byProduct.forEach((id, interaction) ->
        productStats.add(new ProductStats(id, categoryIds.get(id), interaction)));
    List<KeywordStats> keywordStats = byKeyword.entrySet().stream()
        .map(entry -> new KeywordStats(entry.getKey(), entry.getValue())).toList();

    UserEventDailyStats result = new UserEventDailyStats(
        day.toString(), started, RULES_VERSION, totals, productStats, keywordStats);
    Query older = Query.query(Criteria.where("_id").is(result.day())
        .and("calculatedAt").lt(started)
        .and("rulesVersion").is(RULES_VERSION));
    Update summary = new Update().set("calculatedAt", started).set("totals", totals)
        .set("products", productStats).set("keywords", keywordStats);
    if (!historical && mongo.updateFirst(older, summary, UserEventDailyStats.class).getMatchedCount() > 0) {
      return;
    }
    try {
      mongo.insert(result);
    } catch (DuplicateKeyException ignored) {
      // A concurrent refresh may have inserted this day since the first update.
      if (!historical) {
        mongo.updateFirst(older, summary, UserEventDailyStats.class);
      }
    }
  }
}
