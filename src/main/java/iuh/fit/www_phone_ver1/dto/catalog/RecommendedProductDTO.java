package iuh.fit.www_phone_ver1.dto.catalog;

import iuh.fit.www_phone_ver1.entity.Product;
import java.math.BigDecimal;

/** Lightweight DTO returned by the recommendation API without exposing lazy JPA collections. */
public record RecommendedProductDTO(
    Long id,
    String name,
    String brand,
    String kind,
    BigDecimal price,
    BigDecimal originalPrice,
    String image,
    String chipset,
    boolean inStock) {

  public static RecommendedProductDTO from(Product product) {
    return new RecommendedProductDTO(
        product.getId(),
        product.getName(),
        product.getBrand(),
        product.getKind(),
        product.getPrice(),
        product.getOriginalPrice(),
        product.getImage(),
        product.getChipset(),
        product.getStock() > 0);
  }
}
