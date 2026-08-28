-- =====================================================
-- V8: Bổ sung cột kích cỡ (available_sizes) & màu sắc (available_colors) cho bảng products
-- =====================================================

ALTER TABLE products
    ADD COLUMN available_sizes VARCHAR(255) NULL AFTER stock_quantity,
    ADD COLUMN available_colors VARCHAR(255) NULL AFTER available_sizes;
