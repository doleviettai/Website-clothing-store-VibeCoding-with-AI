-- =====================================================
-- V17: Sửa kiểu dữ liệu user_id (BIGINT UNSIGNED) & product_id (BIGINT) tương thích FK chuẩn xác với users & products
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE product_reviews
    MODIFY COLUMN product_id BIGINT NOT NULL,
    MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
