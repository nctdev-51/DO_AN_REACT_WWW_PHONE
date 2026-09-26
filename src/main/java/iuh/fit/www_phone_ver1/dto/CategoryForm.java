package iuh.fit.www_phone_ver1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryForm {
    @NotBlank(message = "Vui lòng nhập tên danh mục")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;

    private String description;
}
