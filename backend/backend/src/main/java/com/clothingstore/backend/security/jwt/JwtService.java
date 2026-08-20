package com.clothingstore.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service xử lý JWT (JSON Web Token).
 * <p>
 * Access token:
 * - Chứa: username (email), issued_at, expiration
 * - Thời hạn ngắn: 15 phút (cấu hình trong application.yaml)
 * - Gửi trong header: Authorization: Bearer <token>
 * <p>
 * Refresh token:
 * - KHÔNG phải JWT — là UUID ngẫu nhiên
 * - Được hash (SHA-256) trước khi lưu DB
 * - Thời hạn dài: 7 ngày
 */
@Service
public class JwtService {

    // Secret key đọc từ application.yaml
    @Value("${jwt.secret}")
    private String secretKey;

    // Thời hạn access token (milliseconds)
    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    // Thời hạn refresh token (milliseconds)
    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /**
     * Tạo access token JWT từ thông tin UserDetails.
     * Username được lưu vào "subject" của JWT.
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Tạo token với claims tùy chỉnh (nếu cần mở rộng).
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())    // subject = email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Lấy username (email) từ JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Kiểm tra token có hợp lệ không:
     * - Username trong token phải khớp với UserDetails
     * - Token chưa hết hạn
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Lấy thời hạn refresh token tính bằng giây (dùng khi tạo RefreshToken entity).
     */
    public long getRefreshTokenExpirySeconds() {
        return refreshTokenExpiry / 1000;
    }

    // ======= Private helpers =======

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
