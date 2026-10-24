package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
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
    if (!enabled || media.count() > 0) return;
    try (var input = getClass().getResourceAsStream("/catalog/media.json")) {
      if (input == null) return;
      for (var group : new JsonMapper().readTree(input)) {
        var product = products.findBySku(group.path("productSku").asString()).orElse(null);
        if (product == null) continue;
        int position = 0;
        for (var image : group.path("images")) {
          ProductMedia entry = new ProductMedia();
          entry.setProduct(product);
          entry.setKind(group.path("kind").asString());
          entry.setColor(group.path("color").isNull() ? null : group.path("color").asString());
          entry.setPath(image.path("path").asString());
          entry.setSource(image.path("source").asString());
          entry.setCaption(group.path("credit").asString(entry.getColor()));
          entry.setSortOrder(position++);
          media.save(entry);
        }
      }
    }
  }
}
