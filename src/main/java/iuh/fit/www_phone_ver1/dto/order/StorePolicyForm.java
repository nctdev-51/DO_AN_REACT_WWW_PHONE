package iuh.fit.www_phone_ver1.dto.order;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class StorePolicyForm {
  @Min(1)
  @Max(30)
  private int returnWindowDays = 7;

  @NotNull private Long version;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("10000000")
  @Digits(integer = 8, fraction = 0)
  private BigDecimal shippingFee;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("1000000000")
  @Digits(integer = 10, fraction = 0)
  private BigDecimal freeShippingThreshold;
}
