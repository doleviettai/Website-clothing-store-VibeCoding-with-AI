package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository cho bảng `refresh_tokens`.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Tìm refresh token theo hash — dùng khi client gửi token để refresh hoặc logout.
     * Client gửi token gốc → backend hash lại → tìm trong DB.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Thu hồi tất cả refresh token của một user — dùng khi đổi mật khẩu, khóa tài khoản.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.user.id = :userId AND r.revokedAt IS NULL")
    void revokeAllByUserId(Long userId, LocalDateTime now);

    /**
     * Xóa các token đã hết hạn — dùng trong scheduled cleanup (tùy chọn).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
