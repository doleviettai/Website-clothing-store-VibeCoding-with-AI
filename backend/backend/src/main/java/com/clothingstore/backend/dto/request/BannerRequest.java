package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO khi Admin tạo mới hoặc cập nhật Banner.
 */
@Getter
@Setter
public class BannerRequest {

    @NotBlank(message = "Tiêu đề banner không được để trống")
    @Size(max = 200, message = "Tiêu đề banner tối đa 200 ký tự")
    private String title;

    private String slug;

    private String description;

    private String imageUrl;

    private String targetUrl;

    @NotBlank(message = "Vị trí banner không được để trống")
    private String position = "HOME_TOP";

    private String status = "ACTIVE";

    private Integer sortOrder = 0;
}
