-- =====================================================
-- V2: Tạo bảng refresh_tokens
-- =====================================================

-- Bảng refresh_tokens: quản lý phiên đăng nhập
-- Database chỉ lưu HASH của refresh token, không lưu token gốc
CREATE TABLE refresh_tokens
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    -- SHA-256 hash của refresh token thực
    token_hash VARCHAR(255)    NOT NULL,
    expires_at DATETIME        NOT NULL,
    -- null nếu chưa bị thu hồi
    revoked_at DATETIME        NULL,
    -- Thông tin thiết bị đăng nhập
    ip_address VARCHAR(45)     NULL,
    user_agent VARCHAR(500)    NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_refresh_tokens_token_hash (token_hash),
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
