package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false, columnDefinition = "bigint default 0")
  private long version;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false, unique = true, length = 120)
  private String sku;

  @Column(nullable = false, length = 80)
  private String color;

  @Column(name = "storage_capacity", nullable = false, length = 40)
  private String storage;

  @Column(length = 40)
  private String ram;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal price;

  @Column(nullable = false)
  private int stock;

  @Column(nullable = false)
  private boolean active = true;

  public String getLabel() {
    return color + " / " + storage + (ram == null || ram.isBlank() ? "" : " / RAM " + ram);
  }

  public String getImage() {
    return product.getMedia().stream()
        .filter(m -> "PHOTO".equals(m.getKind()) && color.equals(m.getColor()))
        .map(ProductMedia::getPath)
        .findFirst()
        .orElse(product.getImage());
  }
}
