package iuh.fit.www_phone_ver1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductForm {
    @NotBlank(message = "Vui lòng nhập tên sản phẩm")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập hãng")
    @Size(max = 100, message = "Tên hãng tối đa 100 ký tự")
    private String brand;

    @NotNull(message = "Vui lòng nhập giá")
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    private Double price;

    private String description;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status = 1;
}
