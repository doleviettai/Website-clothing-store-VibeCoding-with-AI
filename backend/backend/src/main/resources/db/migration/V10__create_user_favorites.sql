-- =====================================================
-- V10: Tạo bảng user_favorites (Sản phẩm yêu thích của khách hàng)
-- =====================================================

CREATE TABLE IF NOT EXISTS user_favorites
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    product_id BIGINT          NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_favorites_user (user_id),
    INDEX idx_favorites_product (product_id),

    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
