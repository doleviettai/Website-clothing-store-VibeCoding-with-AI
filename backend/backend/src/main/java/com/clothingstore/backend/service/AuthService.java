package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.LoginRequest;
import com.clothingstore.backend.dto.request.RefreshTokenRequest;
import com.clothingstore.backend.dto.request.RegisterRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.AuthResponse;
import com.clothingstore.backend.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface định nghĩa các chức năng xác thực.
 * Cài đặt cụ thể nằm trong AuthServiceImpl.
 */
public interface AuthService {

    /**
     * Đăng ký tài khoản mới.
     * Tạo user và gán vai trò CLIENT trong cùng một transaction.
     */
    ApiResponse<UserResponse> register(RegisterRequest request);

    /**
     * Đăng nhập và trả về access token + refresh token.
     *
     * @param httpRequest dùng để lấy IP address và User-Agent
     */
    ApiResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Làm mới access token bằng refresh token.
     */
    ApiResponse<AuthResponse> refresh(RefreshTokenRequest request);

    /**
     * Đăng xuất — thu hồi refresh token.
     */
    ApiResponse<Void> logout(RefreshTokenRequest request);

    /**
     * Lấy thông tin user hiện đang đăng nhập (từ JWT token).
     */
    ApiResponse<UserResponse> getCurrentUser(String email);
}
