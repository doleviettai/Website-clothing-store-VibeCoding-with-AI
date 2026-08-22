package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response DTO trả về thông tin banner.
 */
@Getter
@Builder
public class BannerResponse {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private String position;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
