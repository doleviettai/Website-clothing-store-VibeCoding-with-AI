package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO khi Admin tạo mới hoặc cập nhật Chuyên mục.
 */
@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Tên chuyên mục không được để trống")
    @Size(max = 150, message = "Tên chuyên mục tối đa 150 ký tự")
    private String name;

    // Slug có thể nhập tay hoặc để rỗng để backend tự tạo từ name
    private String slug;

    private Long parentId;

    private String description;

    // URL ảnh hiện tại (nếu sửa và không chọn ảnh mới)
    private String imageUrl;

    private String status = "ACTIVE";

    private Integer sortOrder = 0;
}
