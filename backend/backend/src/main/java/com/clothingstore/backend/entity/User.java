package com.clothingstore.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity ánh xạ với bảng `users`.
 * Lưu thông tin tài khoản của người dùng (cả CLIENT và ADMIN).
 * <p>
 * Chú ý: password_hash — không bao giờ trả trường này về frontend.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email dùng để đăng nhập — không được trùng
    @Column(nullable = false, unique = true, length = 191)
    private String email;

    // Mật khẩu đã được BCrypt hash — KHÔNG bao giờ lưu mật khẩu gốc
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * Trạng thái tài khoản:
     * ACTIVE   — hoạt động bình thường
     * LOCKED   — bị khóa (không thể đăng nhập)
     * INACTIVE — vô hiệu hóa
     */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Xóa mềm: null = chưa xóa, có giá trị = đã xóa
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Quan hệ nhiều-nhiều với Role thông qua bảng user_roles.
     * FetchType.EAGER: load roles ngay khi load user (cần thiết cho Spring Security).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Tự động set thời gian khi tạo mới
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Tự động cập nhật thời gian khi sửa
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
