package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "product_media",
    indexes = @Index(name = "idx_media_product", columnList = "product_id,kind,sort_order"))
@Getter
@Setter
@NoArgsConstructor
public class ProductMedia {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(length = 80)
  private String color;

  @Column(nullable = false, length = 10)
  private String kind = "PHOTO";

  @Column(nullable = false, length = 500)
  private String path;

  @Column(length = 300)
  private String caption;

  @Column(length = 1000)
  private String source;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;
}
