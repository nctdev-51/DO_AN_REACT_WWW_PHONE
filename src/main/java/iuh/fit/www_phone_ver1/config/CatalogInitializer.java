package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
@org.springframework.core.annotation.Order(10)
public class CatalogInitializer implements CommandLineRunner {
  private final CategoryRepository categories;
  private final ProductRepository products;
  private final ProductVariantRepository variants;
  private final boolean enabled;

  public CatalogInitializer(
      CategoryRepository categories,
      ProductRepository products,
      ProductVariantRepository variants,
      @Value("${app.catalog.seed-enabled:true}") boolean enabled) {
    this.categories = categories;
    this.products = products;
    this.variants = variants;
    this.enabled = enabled;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (!enabled || products.count() > 0) return;
    try (var input = getClass().getResourceAsStream("/catalog/products.json")) {
      if (input == null) throw new IllegalStateException("Missing catalog/products.json");
      for (JsonNode node : new JsonMapper().readTree(input)) {
        String brand = node.path("brand").asString();
        String categoryName = node.path("category").asString(brand);
        Category category =
            categories
                .findFirstByName(categoryName)
                .orElseGet(
                    () ->
                        categories.save(
                            Category.builder()
                                .name(categoryName)
                                .description("Điện thoại " + brand)
                                .build()));
        Product product = new Product();
        product.setName(node.path("name").asString());
        product.setBrand(brand);
        product.setUsageTags(node.path("usageTags").asString(""));
        product.setKind(node.path("kind").asString("PHONE"));
        product.setCompatibleSkus(node.path("compatibleSkus").asString(""));
        product.setCategory(category);
        product.setPrice(new BigDecimal(node.path("price").asString()));
        product.setSku(node.path("sku").asString());
        product.setDescription(node.path("description").asString(""));
        product.setImage(node.path("image").asString());
        product.setOriginalPrice(new BigDecimal(node.path("originalPrice").asString()));
        product.setChipset(node.path("chipset").asString(""));
        product.setDisplay(node.path("display").asString(""));
        product.setCamera(node.path("camera").asString(""));
        product.setBattery(node.path("battery").asString(""));
        product.setOperatingSystem(node.path("operatingSystem").asString(""));
        product.setWarrantyMonths(node.path("warrantyMonths").asInt(12));
        product = products.save(product);
        // This initializer runs only for an empty product catalog.
        for (JsonNode variant : node.path("variants")) {
          ProductVariant created = new ProductVariant();
          created.setProduct(product);
          created.setSku(variant.path("sku").asString());
          created.setColor(variant.path("color").asString());
          created.setStorage(variant.path("storage").asString());
          created.setRam(variant.path("ram").asString(""));
          created.setPrice(new BigDecimal(variant.path("price").asString()));
          created.setStock(variant.path("stock").asInt());
          variants.save(created);
        }
      }
    }
  }
}
