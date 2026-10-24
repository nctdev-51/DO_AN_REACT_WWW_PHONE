package iuh.fit.www_phone_ver1.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProfileForm {
  @NotBlank
  @Size(max = 100)
  private String fullName;

  @Pattern(regexp = "^$|0[0-9]{9}", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
  private String phone;
}
