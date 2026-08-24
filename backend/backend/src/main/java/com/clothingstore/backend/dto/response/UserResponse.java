package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO trả về thông tin người dùng cho Admin.
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private Set<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastLogoutAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
