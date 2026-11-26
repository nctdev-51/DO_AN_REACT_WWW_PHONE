package iuh.fit.www_phone_ver1.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
  @NotBlank(message = "Họ tên không được để trống")
  @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
  private String fullName;

  @NotBlank(message = "Email không được để trống")
  @Email(message = "Email không hợp lệ")
  @Size(max = 100, message = "Email tối đa 100 ký tự")
  private String email;

  @NotBlank(message = "Mật khẩu không được để trống")
  @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
  private String password;

  @NotBlank(message = "Xác nhận mật khẩu không được để trống")
  private String confirmPassword;

  @Pattern(regexp = "^$|0[0-9]{9}", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
  private String phone;

  @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
  private String address;
}
