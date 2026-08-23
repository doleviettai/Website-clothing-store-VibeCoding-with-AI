package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO trả về thông tin chi tiết sản phẩm.
 */
@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;

    // Thông tin Chuyên mục
    private Long categoryId;
    private String categoryName;

    // Thông tin Thương hiệu
    private Long brandId;
    private String brandName;

    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private String thumbnailUrl;
    private String shortDescription;
    private String description;
    private String status;
    private Boolean isFeatured;
    private Integer favoriteCount;
    private Integer reviewCount;
    private BigDecimal averageRating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
