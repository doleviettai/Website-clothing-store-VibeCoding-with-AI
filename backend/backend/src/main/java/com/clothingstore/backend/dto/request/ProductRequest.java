package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO khi Admin tạo mới hoặc cập nhật Sản phẩm.
 */
@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    private String slug;

    private Long categoryId;

    private Long brandId;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    private BigDecimal salePrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity = 0;

    private String thumbnailUrl;

    private String shortDescription;

    private String description;

    private String status = "ACTIVE";

    private Boolean isFeatured = false;

    private Integer favoriteCount = 0;

    private BigDecimal averageRating = new BigDecimal("5.00");
}
