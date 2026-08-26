-- =====================================================
-- V11: Tạo bảng product_reviews (Đánh giá & Bình luận sản phẩm)
-- =====================================================

CREATE TABLE product_reviews
(
    id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id           BIGINT UNSIGNED NOT NULL,
    user_id              BIGINT UNSIGNED NOT NULL,
    order_item_id        BIGINT UNSIGNED NULL,
    rating               TINYINT UNSIGNED NOT NULL DEFAULT 5,
    content              TEXT             NULL,
    status               VARCHAR(30)      NOT NULL DEFAULT 'VISIBLE',
    is_verified_purchase TINYINT(1)       NOT NULL DEFAULT 1,
    created_at           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at           DATETIME         NULL,

    PRIMARY KEY (id),
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_user (user_id),
    INDEX idx_reviews_status (status),
    INDEX idx_reviews_created (created_at),

    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Chèn dữ liệu đánh giá sản phẩm mẫu để thử nghiệm
INSERT INTO product_reviews (product_id, user_id, rating, content, status, is_verified_purchase, created_at, updated_at)
SELECT p.id, u.id, 5, 'Sản phẩm giao hàng nhanh, chất liệu vải mềm mại mặc rất thoải mái!', 'VISIBLE', 1, NOW(), NOW()
FROM products p, users u
WHERE u.email = 'admin@gmail.com'
LIMIT 1;

INSERT INTO product_reviews (product_id, user_id, rating, content, status, is_verified_purchase, created_at, updated_at)
SELECT p.id, u.id, 4, 'Đóng gói cẩn thận, đúng form dáng như hình mô tả. Sẽ ủng hộ shop tiếp!', 'VISIBLE', 1, NOW(), NOW()
FROM products p, users u
WHERE u.email = 'doleviettai231105@gmail.com'
LIMIT 1;
