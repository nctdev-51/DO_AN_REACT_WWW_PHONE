package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
public class Promotion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private long version;

  @Column(nullable = false, unique = true, length = 40)
  private String code;

  @Column(nullable = false, length = 200)
  private String description;

  @Column(length = 1000)
  private String requiredSkus;

  @Column(length = 100)
  private String scopeBrand;

  @Column(length = 1000)
  private String scopeSkus;

  public String getEndsAtLabel() {
    return endsAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
  }

  public String getScopeLabel() {
    return scopeSkus != null && !scopeSkus.isBlank()
        ? "Áp dụng cho sản phẩm trong chương trình"
        : scopeBrand != null && !scopeBrand.isBlank()
            ? "Sản phẩm " + scopeBrand
            : "Toàn bộ sản phẩm";
  }

  @Column(nullable = false, length = 10)
  private String discountType;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal amount;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal minimumSubtotal = BigDecimal.ZERO;

  @Column(precision = 19, scale = 0)
  private BigDecimal maximumDiscount;

  @Column(nullable = false)
  private LocalDateTime startsAt;

  @Column(nullable = false)
  private LocalDateTime endsAt;

  @Column(nullable = false)
  private int usageLimit = 100;

  @Column(nullable = false)
  private boolean active = true;
}
