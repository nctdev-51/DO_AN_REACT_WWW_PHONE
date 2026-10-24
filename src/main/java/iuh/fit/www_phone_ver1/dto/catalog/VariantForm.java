package iuh.fit.www_phone_ver1.dto.catalog;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class VariantForm {
  private Long version;

  @NotBlank
  @Size(max = 120)
  private String sku;

  @NotBlank
  @Size(max = 80)
  private String color;

  @NotBlank
  @Size(max = 40)
  private String storage;

  @Size(max = 40)
  private String ram;

  @NotNull
  @DecimalMin("1")
  @Digits(integer = 10, fraction = 0)
  @DecimalMax("1000000000")
  private BigDecimal price;

  @Min(0)
  @Max(1000000)
  private int stock;

  private boolean active = true;
}
