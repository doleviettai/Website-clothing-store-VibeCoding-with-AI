-- =====================================================
-- V6: Tạo bảng banners (Banner quảng cáo & Slide)
-- =====================================================

CREATE TABLE banners
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200)    NOT NULL,
    slug       VARCHAR(191)    NOT NULL,
    description TEXT           NULL,
    image_url  VARCHAR(500)    NOT NULL,
    target_url VARCHAR(500)    NULL,
    position   VARCHAR(50)     NOT NULL DEFAULT 'HOME_TOP',
    status     VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    sort_order INT             NOT NULL DEFAULT 0,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME        NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_banners_slug (slug),
    INDEX idx_banners_position_status (position, status),
    INDEX idx_banners_sort_order (sort_order)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
