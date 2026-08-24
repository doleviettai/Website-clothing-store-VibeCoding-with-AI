package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.LoginRequest;
import com.clothingstore.backend.dto.request.RefreshTokenRequest;
import com.clothingstore.backend.dto.request.RegisterRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.AuthResponse;
import com.clothingstore.backend.dto.response.UserResponse;
import com.clothingstore.backend.entity.RefreshToken;
import com.clothingstore.backend.entity.Role;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.RefreshTokenRepository;
import com.clothingstore.backend.repository.RoleRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.security.jwt.JwtService;
import com.clothingstore.backend.service.AuthService;
import com.clothingstore.backend.util.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cài đặt AuthService.
 * Đã tách UserDetailsService ra CustomUserDetailsService để tránh vòng lặp phụ thuộc.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Đăng ký tài khoản mới.
     */
    @Override
    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest request) {

        // 1. Kiểm tra mật khẩu và xác nhận mật khẩu khớp nhau
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp");
        }

        // 2. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }

        // 3. Kiểm tra số điện thoại (nếu có)
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng");
        }

        // 4. Lấy vai trò CLIENT từ database
        Role clientRole = roleRepository.findByCode("CLIENT")
                .orElseThrow(() -> new AppException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Vai trò CLIENT không tồn tại trong hệ thống"
                ));

        // 5. Tạo user mới — mật khẩu phải được hash BCrypt
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status("ACTIVE")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        // 6. Lưu vào database
        userRepository.save(user);

        return ApiResponse.success("Đăng ký thành công", toUserResponse(user));
    }

    /**
     * Đăng nhập — trả về access token + refresh token.
     */
    @Override
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpRequest) {

        // 1. Tìm user theo email (chỉ user chưa xóa mềm)
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "Email hoặc mật khẩu không chính xác"
                ));

        // 2. So sánh mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
        }

        // 3. Kiểm tra trạng thái tài khoản
        if ("LOCKED".equals(user.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ");
        }

        // 4. Cập nhật thời gian đăng nhập cuối
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Tạo access token (JWT)
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);

        // 6. Tạo refresh token
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = TokenHashUtil.hash(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirySeconds()))
                .ipAddress(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();
        refreshTokenRepository.save(refreshToken);

        // 7. Trả về response
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .user(toUserResponse(user))
                .build();

        return ApiResponse.success("Đăng nhập thành công", authResponse);
    }

    /**
     * Làm mới access token bằng refresh token.
     */
    @Override
    @Transactional
    public ApiResponse<AuthResponse> refresh(RefreshTokenRequest request) {

        // 1. Hash token client gửi lên để tìm trong DB
        String tokenHash = TokenHashUtil.hash(request.getRefreshToken());

        // 2. Tìm token trong DB
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "Refresh token không hợp lệ"
                ));

        // 3. Kiểm tra token còn hợp lệ không
        if (!refreshToken.isValid()) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn hoặc bị thu hồi");
        }

        // 4. Tạo access token mới
        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .user(toUserResponse(user))
                .build();

        return ApiResponse.success("Làm mới token thành công", authResponse);
    }

    /**
     * Đăng xuất — thu hồi refresh token.
     */
    @Override
    @Transactional
    public ApiResponse<Void> logout(RefreshTokenRequest request) {

        String tokenHash = TokenHashUtil.hash(request.getRefreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });

        return ApiResponse.success("Đăng xuất thành công");
    }

    /**
     * Lấy thông tin user đang đăng nhập từ email.
     */
    @Override
    public ApiResponse<UserResponse> getCurrentUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        return ApiResponse.success("Lấy thông tin thành công", toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        Set<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roles(roleCodes)
                .build();
    }
}
