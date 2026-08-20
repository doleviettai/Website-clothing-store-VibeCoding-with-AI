package com.clothingstore.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ với bảng `refresh_tokens`.
 * Lưu phiên đăng nhập của người dùng.
 * <p>
 * Bảo mật: database chỉ lưu HASH của token (SHA-256), không lưu token gốc.
 * Nếu database bị lộ, kẻ tấn công không thể dùng hash để giả mạo.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ với user — mỗi token thuộc về một user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SHA-256 hash của refresh token thực gửi cho client
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    // Thời điểm token hết hạn
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // null = chưa bị thu hồi; có giá trị = đã logout hoặc bị admin thu hồi
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    // Thông tin thiết bị (để tracking và bảo mật)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra token còn hợp lệ không.
     * Token hợp lệ khi: chưa hết hạn VÀ chưa bị thu hồi.
     */
    public boolean isValid() {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }
}
