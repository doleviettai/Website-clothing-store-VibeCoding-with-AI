-- =====================================================
-- V15: Bổ sung cột last_logout_at cho bảng users
-- =====================================================

ALTER TABLE users
    ADD COLUMN last_logout_at DATETIME NULL AFTER last_login_at;
