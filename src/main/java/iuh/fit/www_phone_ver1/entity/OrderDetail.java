package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal price;

  @Column(length = 200)
  private String productName;

  @Column(length = 200)
  private String variantLabel;

  @Column(length = 120)
  private String sku;

  private Integer warrantyMonths;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id")
  private ProductVariant variant;

  public String getDisplayName() {
    return productName == null ? product.getName() : productName;
  }

  public BigDecimal getSubTotal() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Product product;
}
