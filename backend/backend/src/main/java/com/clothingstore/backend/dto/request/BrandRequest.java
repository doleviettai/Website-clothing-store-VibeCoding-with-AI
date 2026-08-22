package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO khi Admin tạo mới hoặc cập nhật Thương hiệu.
 */
@Getter
@Setter
public class BrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 150, message = "Tên thương hiệu tối đa 150 ký tự")
    private String name;

    private String slug;

    private String description;

    private String logoUrl;

    private String status = "ACTIVE";
}
