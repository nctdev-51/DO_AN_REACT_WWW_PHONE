package iuh.fit.www_phone_ver1.dto.order;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SupportRequestForm {
  @NotNull private Long detailId;

  @NotNull
  @Pattern(regexp = "RETURN|WARRANTY")
  private String type;

  @Min(1)
  @Max(99)
  private int quantity = 1;

  @NotBlank
  @Size(min = 10, max = 2000)
  private String reason;
}
