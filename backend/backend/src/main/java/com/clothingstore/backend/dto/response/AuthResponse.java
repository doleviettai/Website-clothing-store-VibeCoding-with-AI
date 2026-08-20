package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response trả về sau khi đăng nhập hoặc refresh token thành công.
 * Chứa cả access token, refresh token và thông tin user.
 */
@Getter
@Builder
public class AuthResponse {

    // JWT access token — thời hạn ngắn (15 phút)
    // Frontend gắn vào header: Authorization: Bearer <accessToken>
    private String accessToken;

    // Refresh token — thời hạn dài (7 ngày)
    // Dùng để lấy access token mới khi access token hết hạn
    private String refreshToken;

    // Thông tin user để frontend lưu vào store
    private UserResponse user;
}
