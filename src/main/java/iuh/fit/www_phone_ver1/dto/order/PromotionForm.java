package iuh.fit.www_phone_ver1.dto.order;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class PromotionForm {
  private Long version;

  @NotBlank
  @Pattern(
      regexp = "[A-Za-z0-9_-]{3,40}",
      message = "Mã gồm 3–40 chữ cái, chữ số, dấu gạch ngang hoặc gạch dưới")
  private String code;

  @NotBlank
  @Size(max = 200)
  private String description;

  @Size(max = 1000)
  @Pattern(regexp = "[A-Za-z0-9_, -]*")
  private String requiredSkus;

  @Size(max = 100)
  private String scopeBrand;

  @Size(max = 1000)
  @Pattern(regexp = "[A-Za-z0-9_, -]*")
  private String scopeSkus;

  @NotNull
  @Pattern(regexp = "FIXED|PERCENT")
  private String discountType = "FIXED";

  @NotNull
  @DecimalMin("1")
  @DecimalMax("100000000")
  @Digits(integer = 9, fraction = 0)
  private BigDecimal amount;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("1000000000")
  @Digits(integer = 10, fraction = 0)
  private BigDecimal minimumSubtotal = BigDecimal.ZERO;

  @DecimalMin("1")
  @DecimalMax("100000000")
  @Digits(integer = 9, fraction = 0)
  private BigDecimal maximumDiscount;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime startsAt = LocalDateTime.now().withSecond(0).withNano(0);

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime endsAt = LocalDateTime.now().plusDays(30).withSecond(0).withNano(0);

  @Min(1)
  @Max(1000000)
  private int usageLimit = 100;

  private boolean active = true;
}
