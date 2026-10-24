package iuh.fit.www_phone_ver1.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddressForm {
  @Size(max = 10)
  private String provinceCode;

  @Size(max = 10)
  private String wardCode;

  @Pattern(regexp = "structured|legacy")
  private String addressMode = "legacy";

  @Size(max = 100)
  private String province;

  @Size(max = 100)
  private String ward;

  @NotBlank
  @Size(max = 100)
  private String recipientName;

  @NotBlank
  @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
  private String phone;

  @NotBlank
  @Size(max = 255)
  private String line;

  private boolean defaultAddress;
}
