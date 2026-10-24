package iuh.fit.www_phone_ver1.dto.order;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ConsultationForm {
  @NotNull private Long productId;

  @NotBlank
  @Pattern(regexp = "INSTALLMENT|TRADE_IN")
  private String type = "INSTALLMENT";

  @NotBlank
  @Size(max = 100)
  private String contactName;

  @NotBlank
  @Pattern(regexp = "0[0-9]{9}")
  private String phone;

  @NotBlank
  @Size(min = 10, max = 1500)
  private String message;
}
