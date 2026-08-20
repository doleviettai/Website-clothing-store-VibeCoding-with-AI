package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Thông tin cơ bản của user trả về frontend.
 * KHÔNG bao gồm: passwordHash, deletedAt.
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String status;
    // Danh sách mã vai trò: ["CLIENT"] hoặc ["ADMIN"]
    private List<String> roles;
}
