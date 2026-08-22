package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response DTO trả về thông tin thương hiệu.
 */
@Getter
@Builder
public class BrandResponse {

    private Long id;
    private String name;
    private String slug;
    private String logoUrl;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
