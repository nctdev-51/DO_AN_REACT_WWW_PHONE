package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;

/** Editable storefront content and reward rules. Text only; never interpreted as markup. */
@Entity
@Table(name = "store_experience")
@Getter
@Setter
@NoArgsConstructor
public class StoreExperience {
  @Id private Long id = 1L;
  @Version private long version;

  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  private String headline = "Chọn công nghệ. Theo cách của bạn.";

  @Size(max = 240)
  @Column(length = 240)
  private String introduction =
      "Khám phá điện thoại, so sánh cấu hình và tìm lựa chọn vừa ngân sách.";

  @Size(max = 80)
  @Column(length = 80)
  private String heroSku = "PS-IPHONE-17-PRO-MAX";

  @Size(max = 80)
  @Column(length = 80)
  private String secondHeroSku = "PS-SAMSUNG-GALAXY-S25-ULTRA";

  @Size(max = 1000)
  @Column(length = 1000)
  private String featuredSkus = "";

  @Email
  @Size(max = 150)
  @Column(length = 150)
  private String supportEmail = "";

  @Pattern(regexp = "[0-9+ ()-]{0,30}")
  @Column(length = 30)
  private String hotline = "";

  @Size(max = 255)
  @Column(length = 255)
  private String storeAddress = "";

  @Column(nullable = false, columnDefinition = "boolean default true")
  private boolean loyaltyEnabled = true;

  @NotNull
  @DecimalMin("1000")
  @DecimalMax("1000000000")
  @Digits(integer = 10, fraction = 0)
  @Column(
      nullable = false,
      precision = 19,
      scale = 0,
      columnDefinition = "decimal(19,0) default 100000")
  private BigDecimal earnEvery = BigDecimal.valueOf(100000);

  @NotNull
  @DecimalMin("1")
  @DecimalMax("1000000")
  @Digits(integer = 7, fraction = 0)
  @Column(
      nullable = false,
      precision = 19,
      scale = 0,
      columnDefinition = "decimal(19,0) default 1000")
  private BigDecimal pointValue = BigDecimal.valueOf(1000);

  @Min(1)
  @Max(50)
  @Column(nullable = false, columnDefinition = "integer default 10")
  private int redeemPercent = 10;

  @Min(1)
  @Max(30)
  @Column(nullable = false, columnDefinition = "integer default 2")
  private int deliveryMinDays = 2;

  @Min(1)
  @Max(60)
  @Column(nullable = false, columnDefinition = "integer default 5")
  private int deliveryMaxDays = 5;
}
