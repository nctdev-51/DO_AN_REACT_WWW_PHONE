package iuh.fit.www_phone_ver1.dto.order;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ParcelForm {
  @Min(1)
  @Max(19999)
  private int weight = 500;

  @Min(1)
  @Max(200)
  private int length = 25;

  @Min(1)
  @Max(200)
  private int width = 20;

  @Min(1)
  @Max(200)
  private int height = 10;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("5000000")
  @Digits(integer = 7, fraction = 0)
  private BigDecimal insuranceValue = BigDecimal.ZERO;
}
