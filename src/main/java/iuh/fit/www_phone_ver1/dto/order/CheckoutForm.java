package iuh.fit.www_phone_ver1.dto.order;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CheckoutForm {
  @Size(max = 10)
  private String provinceCode;

  @Size(max = 10)
  private String wardCode;

  @Pattern(regexp = "structured|legacy")
  private String addressMode = "legacy";

  @Min(0)
  @Max(1000000)
  private long redeemPoints;

  @Size(max = 100)
  private String province;

  @Size(max = 100)
  private String ward;

  @Size(max = 40, message = "Mã giảm giá tối đa 40 ký tự")
  private String promotionCode;

  @NotBlank(message = "Vui lòng nhập tên người nhận")
  @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
  private String recipientName;

  @Pattern(regexp = "COD|VNPAY", message = "Phương thức thanh toán không hợp lệ")
  @NotBlank
  private String paymentMethod = "COD";

  @NotBlank(message = "Vui lòng nhập địa chỉ nhận hàng")
  @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
  private String shippingAddress;

  @NotBlank(message = "Vui lòng nhập số điện thoại")
  @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
  private String phone;
}
