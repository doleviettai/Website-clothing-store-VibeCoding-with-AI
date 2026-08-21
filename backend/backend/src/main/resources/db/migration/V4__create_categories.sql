-- =====================================================
-- V4: Tạo bảng categories (Chuyên mục sản phẩm)
-- =====================================================

CREATE TABLE categories
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT UNSIGNED NULL,
    name        VARCHAR(150)    NOT NULL,
    slug        VARCHAR(191)    NOT NULL,
    description TEXT            NULL,
    image_url   VARCHAR(500)    NULL,
    status      VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME        NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_categories_slug (slug),
    INDEX idx_categories_parent_id (parent_id),
    INDEX idx_categories_status_sort_order (status, sort_order),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
