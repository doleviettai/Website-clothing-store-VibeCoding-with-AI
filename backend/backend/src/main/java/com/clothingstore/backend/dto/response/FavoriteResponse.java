package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FavoriteResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String brandName;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String thumbnailUrl;
    private BigDecimal averageRating;
    private LocalDateTime createdAt;
}
