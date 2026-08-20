package com.clothingstore.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ với bảng `roles`.
 * Định nghĩa các vai trò trong hệ thống: ADMIN, CLIENT.
 * Có thể mở rộng thêm vai trò khác trong tương lai (STAFF, ORDER_MANAGER...).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã vai trò dùng trong code: "ADMIN", "CLIENT"
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    // Tên hiển thị: "Quản trị viên", "Khách hàng"
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
