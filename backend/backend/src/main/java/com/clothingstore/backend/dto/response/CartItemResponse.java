package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String brandName;
    private String categoryName;
    private String thumbnailUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal itemTotal;
    private String size;
    private String color;
    private Integer stockQuantity;
}
