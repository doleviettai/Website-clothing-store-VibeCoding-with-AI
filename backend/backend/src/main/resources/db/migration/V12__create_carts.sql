-- =====================================================
-- V12: Tạo bảng carts và cart_items (Giỏ hàng & chi tiết giỏ hàng khách hàng)
-- =====================================================

CREATE TABLE IF NOT EXISTS carts
(
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_carts_user (user_id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart_items
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    cart_id    BIGINT          NOT NULL,
    product_id BIGINT          NOT NULL,
    quantity   INT             NOT NULL DEFAULT 1,
    price      DECIMAL(15, 2)  NOT NULL,
    size       VARCHAR(50)     NULL,
    color      VARCHAR(50)     NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_cart_items_cart (cart_id),
    INDEX idx_cart_items_product (product_id),

    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
