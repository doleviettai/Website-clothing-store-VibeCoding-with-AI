package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.LoginRequest;
import com.clothingstore.backend.dto.request.RefreshTokenRequest;
import com.clothingstore.backend.dto.request.RegisterRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.AuthResponse;
import com.clothingstore.backend.dto.response.UserResponse;
import com.clothingstore.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API xác thực.
 * <p>
 * Tất cả endpoint đều public (không cần token), trừ /me.
 * Base path: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Đăng ký tài khoản mới.
     * Request body: { fullName, email, phone, password, confirmPassword }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        ApiResponse<UserResponse> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/auth/login
     * Đăng nhập — trả về access token và refresh token.
     * Request body: { email, password }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest   // Để lấy IP, User-Agent
    ) {
        ApiResponse<AuthResponse> response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/auth/refresh
     * Làm mới access token bằng refresh token.
     * Request body: { refreshToken }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        ApiResponse<AuthResponse> response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/auth/logout
     * Đăng xuất — thu hồi refresh token.
     * Request body: { refreshToken }
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        ApiResponse<Void> response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/auth/me
     * Lấy thông tin user đang đăng nhập.
     * Yêu cầu: Authorization: Bearer <access_token>
     *
     * @AuthenticationPrincipal: Spring Security tự inject UserDetails từ SecurityContext.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ApiResponse<UserResponse> response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
