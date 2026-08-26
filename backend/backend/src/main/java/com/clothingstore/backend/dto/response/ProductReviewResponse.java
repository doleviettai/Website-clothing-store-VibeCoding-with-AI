package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response DTO trả về thông tin chi tiết đánh giá sản phẩm cho Admin.
 */
@Getter
@Builder
public class ProductReviewResponse {

    private Long id;

    // Thông tin sản phẩm
    private Long productId;
    private String productName;
    private String productThumbnailUrl;

    // Thông tin người đánh giá
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userAvatarUrl;

    private Integer rating;
    private String content;
    private String status;
    private Boolean isVerifiedPurchase;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
