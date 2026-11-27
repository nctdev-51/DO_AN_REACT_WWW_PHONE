package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class HomePersonalization {
  public enum Interest {
    CAMERA, GAMING, BUDGET, DEFAULT;

    public boolean matches(Product product) {
      if (product == null || !"PHONE".equals(product.getKind())) return false;
      String tags = Objects.toString(product.getUsageTags(), "").toUpperCase(Locale.ROOT);
      return switch (this) {
        case CAMERA -> tags.contains("CAMERA") && !BUDGET.matches(product);
        case GAMING -> tags.contains("GAMING") || tags.contains("BATTERY");
        case BUDGET -> product.getPrice() != null
            && product.getPrice().compareTo(BigDecimal.valueOf(10_000_000)) <= 0;
        case DEFAULT -> false;
      };
    }
  }

  private final MongoTemplate mongo;
  private final ProductRepository products;

  public HomePersonalization(MongoTemplate mongo, ProductRepository products) {
    this.mongo = mongo;
    this.products = products;
  }

  public Interest interest(String visitorId) {
    if (visitorId == null) return Interest.DEFAULT;
    List<UserEvent> history;
    try {
      history = mongo.find(
          Query.query(Criteria.where("sessionId").is(visitorId)
                  .and("eventType").in("PRODUCT_CLICK", "PAGE_VIEW", "SEARCH")
                  .and("createdAt").gte(LocalDateTime.now().minusDays(30)))
              .with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(30),
          UserEvent.class);
    } catch (DataAccessException ex) {
      return Interest.DEFAULT;
    }
    var byId = new HashMap<Long, Product>();
    products.findAllById(history.stream().map(UserEvent::getProductId)
            .filter(Objects::nonNull).distinct().toList())
        .forEach(product -> byId.put(product.getId(), product));
    var scores = new java.util.EnumMap<Interest, Integer>(Interest.class);
    for (UserEvent event : history) {
      String keyword = Objects.toString(event.getKeyword(), "").toLowerCase(Locale.ROOT);
      Product product = byId.get(event.getProductId());
      int weight = "PRODUCT_CLICK".equals(event.getEventType()) ? 2 : 1;
      add(scores, Interest.CAMERA, product, keyword, "camera|chụp ảnh|iphone|ultra", weight);
      add(scores, Interest.GAMING, product, keyword, "game|pin trâu|poco|rog phone", weight);
      add(scores, Interest.BUDGET, product, keyword, "dưới 10|giá rẻ", weight);
    }
    return List.of(Interest.CAMERA, Interest.GAMING, Interest.BUDGET).stream()
        .filter(scores::containsKey)
        .max(java.util.Comparator.comparingInt(scores::get))
        .orElse(Interest.DEFAULT);
  }

  private void add(java.util.Map<Interest, Integer> scores, Interest interest,
      Product product, String keyword, String terms, int weight) {
    if (interest.matches(product) || java.util.Arrays.stream(terms.split("\\|"))
        .anyMatch(keyword::contains)) {
      scores.merge(interest, weight, Integer::sum);
    }
  }
}
