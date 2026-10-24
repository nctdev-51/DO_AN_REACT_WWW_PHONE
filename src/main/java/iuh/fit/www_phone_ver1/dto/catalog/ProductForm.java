package iuh.fit.www_phone_ver1.dto.catalog;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductForm {
  @Size(max = 200)
  @jakarta.validation.constraints.Pattern(
      regexp =
          "(?:EVERYDAY|CAMERA|GAMING|BATTERY|COMPACT)(?:,(?:EVERYDAY|CAMERA|GAMING|BATTERY|COMPACT))*|",
      message = "Nhóm nhu cầu không hợp lệ")
  private String usageTags = "";

  @NotBlank
  @jakarta.validation.constraints.Pattern(regexp = "PHONE|ACCESSORY")
  private String kind = "PHONE";

  @Size(max = 1500)
  @jakarta.validation.constraints.Pattern(regexp = "[A-Za-z0-9_, -]*")
  private String compatibleSkus;

  @NotBlank(message = "Vui lòng nhập tên sản phẩm")
  @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
  private String name;

  @NotBlank(message = "Vui lòng nhập hãng")
  @Size(max = 100, message = "Tên hãng tối đa 100 ký tự")
  private String brand;

  @NotNull(message = "Vui lòng nhập giá")
  @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
  @Digits(integer = 10, fraction = 0, message = "Giá phải là số nguyên VNĐ")
  @DecimalMax(value = "1000000000", message = "Giá tối đa 1 tỷ đồng")
  private BigDecimal price;

  @Size(max = 80)
  @jakarta.validation.constraints.Pattern(
      regexp = "[A-Za-z0-9_-]*",
      message = "SKU chỉ gồm chữ, số, gạch ngang và gạch dưới")
  private String sku;

  @Size(max = 200)
  private String chipset;

  @Size(max = 200)
  private String display;

  @Size(max = 200)
  private String camera;

  @Size(max = 120)
  private String battery;

  @Size(max = 100)
  private String operatingSystem;

  @Min(0)
  @Max(60)
  private Integer warrantyMonths = 12;

  private String description;

  @NotNull(message = "Vui lòng chọn danh mục")
  private Long categoryId;

  @NotNull
  @Min(0)
  @Max(1)
  private Integer status = 1;
}
