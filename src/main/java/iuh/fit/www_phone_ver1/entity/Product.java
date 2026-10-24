package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 100)
  private String brand;

  @Column(nullable = false, length = 12, columnDefinition = "varchar(12) default 'PHONE'")
  @Builder.Default
  private String kind = "PHONE";

  @Column(length = 1500)
  private String compatibleSkus;

  @Column(length = 200)
  private String usageTags;

  public ProductVariant getDefaultVariant() {
    return variants.stream()
        .filter(ProductVariant::isActive)
        .filter(v -> v.getStock() > 0)
        .findFirst()
        .orElseGet(
            () -> variants.stream().filter(ProductVariant::isActive).findFirst().orElse(null));
  }

  @Column(length = 255)
  private String image;

  @Column(name = "sku", unique = true, length = 80)
  private String sku;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal price;

  @Column(precision = 19, scale = 0)
  private BigDecimal originalPrice;

  @Column(length = 200)
  private String chipset;

  @Column(length = 200)
  private String display;

  @Column(length = 200)
  private String camera;

  @Column(length = 120)
  private String battery;

  @Column(length = 100)
  private String operatingSystem;

  @Builder.Default private Integer warrantyMonths = 12;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 32)
  @OrderBy("price ASC, id ASC")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<ProductVariant> variants = new ArrayList<>();

  @OneToMany(mappedBy = "product")
  @BatchSize(size = 32)
  @OrderBy("sortOrder ASC, id ASC")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<ProductMedia> media = new ArrayList<>();

  public int getStock() {
    return variants.stream()
        .filter(ProductVariant::isActive)
        .mapToInt(ProductVariant::getStock)
        .sum();
  }

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  @Builder.Default
  private Integer status = 1; // 1: Đang bán, 0: Ngừng kinh doanh

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Category category;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<OrderDetail> orderDetails = new ArrayList<>();
}
