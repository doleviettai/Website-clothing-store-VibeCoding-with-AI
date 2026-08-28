-- =====================================================
-- V18: Đồng bộ chuẩn hóa kiểu dữ liệu cho carts, cart_items và user_favorites
-- Quy tắc: Chỉ user_id (trỏ users.id) dùng BIGINT UNSIGNED; cart_id & product_id dùng BIGINT (SIGNED)
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE carts
    MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE cart_items
    MODIFY COLUMN cart_id BIGINT NOT NULL,
    MODIFY COLUMN product_id BIGINT NOT NULL;

ALTER TABLE user_favorites
    MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL,
    MODIFY COLUMN product_id BIGINT NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
