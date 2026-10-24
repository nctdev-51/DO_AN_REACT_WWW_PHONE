package iuh.fit.www_phone_ver1.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PasswordForm {
  @NotBlank private String currentPassword;

  @NotBlank
  @Size(min = 8, max = 72, message = "Mật khẩu mới phải từ 8 đến 72 ký tự")
  private String newPassword;

  @NotBlank private String confirmPassword;
}
