package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@org.springframework.core.annotation.Order(20)
public class ProductMediaInitializer implements CommandLineRunner {
  private final ProductMediaRepository media;
  private final ProductRepository products;
  private final boolean enabled;

  public ProductMediaInitializer(
      ProductMediaRepository media,
      ProductRepository products,
      @Value("${app.catalog.seed-enabled:true}") boolean enabled) {
    this.media = media;
    this.products = products;
    this.enabled = enabled;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (!enabled) return;
    try (var input = getClass().getResourceAsStream("/catalog/media.json")) {
      if (input == null) return;
      Map<Long, Set<String>> existingKeysByProduct = new HashMap<>();
      Map<Long, Integer> nextSortByProduct = new HashMap<>();
      for (var group : new JsonMapper().readTree(input)) {
        var product = products.findBySku(group.path("productSku").asString()).orElse(null);
        if (product == null) continue;
        Long productId = product.getId();
        if (!existingKeysByProduct.containsKey(productId)) {
          var existingEntries = media.findByProduct_IdOrderBySortOrderAscIdAsc(productId);
          Set<String> keys = new HashSet<>();
          int maxOrder = -1;
          for (var existing : existingEntries) {
            keys.add(mediaKey(existing.getKind(), existing.getColor(), existing.getPath()));
            if (existing.getSortOrder() > maxOrder) maxOrder = existing.getSortOrder();
          }
          existingKeysByProduct.put(productId, keys);
          nextSortByProduct.put(productId, maxOrder + 1);
        }
        Set<String> existingKeys = existingKeysByProduct.get(productId);
        int position = nextSortByProduct.get(productId);
        String kind = group.path("kind").asString();
        String color = group.path("color").isNull() ? null : group.path("color").asString();
        String caption = group.path("credit").asString(color);
        for (var image : group.path("images")) {
          String path = image.path("path").asString();
          String key = mediaKey(kind, color, path);
          if (!existingKeys.add(key)) continue;
          ProductMedia entry = new ProductMedia();
          entry.setProduct(product);
          entry.setKind(kind);
          entry.setColor(color);
          entry.setPath(path);
          entry.setSource(image.path("source").asString());
          entry.setCaption(caption);
          entry.setSortOrder(position++);
          media.save(entry);
        }
        nextSortByProduct.put(productId, position);
      }
    }
  }

  private static String mediaKey(String kind, String color, String path) {
    return Objects.toString(kind, "")
        + "::"
        + Objects.toString(color, "")
        + "::"
        + Objects.toString(path, "");
  }
}
