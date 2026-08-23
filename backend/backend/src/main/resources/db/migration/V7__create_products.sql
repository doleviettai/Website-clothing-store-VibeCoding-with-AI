-- =====================================================
-- V7: Tạo bảng products (Quản lý sản phẩm)
-- =====================================================

CREATE TABLE products
(
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id       BIGINT UNSIGNED NULL,
    brand_id          BIGINT UNSIGNED NULL,
    name              VARCHAR(255)    NOT NULL,
    slug              VARCHAR(191)    NOT NULL,
    price             DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    sale_price        DECIMAL(12,2)   NULL,
    stock_quantity    INT             NOT NULL DEFAULT 0,
    thumbnail_url     VARCHAR(500)    NULL,
    short_description VARCHAR(500)    NULL,
    description       LONGTEXT        NULL,
    status            VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    is_featured       TINYINT(1)      NOT NULL DEFAULT 0,
    favorite_count    INT             NOT NULL DEFAULT 0,
    review_count      INT             NOT NULL DEFAULT 0,
    average_rating    DECIMAL(3,2)    NOT NULL DEFAULT 5.00,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        DATETIME        NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_products_slug (slug),
    INDEX idx_products_category (category_id),
    INDEX idx_products_brand (brand_id),
    INDEX idx_products_name (name),
    INDEX idx_products_status (status),
    INDEX idx_products_price (price),

    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
