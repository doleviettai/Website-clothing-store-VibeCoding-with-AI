-- =====================================================
-- V5: Tạo bảng brands (Thương hiệu sản phẩm)
-- =====================================================

CREATE TABLE brands
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150)    NOT NULL,
    slug        VARCHAR(191)    NOT NULL,
    logo_url    VARCHAR(500)    NULL,
    description TEXT            NULL,
    status      VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME        NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_brands_slug (slug),
    INDEX idx_brands_name (name),
    INDEX idx_brands_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
