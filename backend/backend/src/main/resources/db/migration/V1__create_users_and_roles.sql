-- =====================================================
-- V1: Tạo bảng users, roles, user_roles
-- =====================================================

-- Bảng users: lưu thông tin tài khoản người dùng
CREATE TABLE users
(
    id                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    email             VARCHAR(191)     NOT NULL,
    password_hash     VARCHAR(255)     NOT NULL,
    full_name         VARCHAR(100)     NOT NULL,
    phone             VARCHAR(20)      NULL,
    avatar_url        VARCHAR(500)     NULL,
    -- PENDING: mới tạo, ACTIVE: hoạt động, LOCKED: bị khóa, INACTIVE: vô hiệu hóa
    status            VARCHAR(30)      NOT NULL DEFAULT 'ACTIVE',
    email_verified_at DATETIME         NULL,
    last_login_at     DATETIME         NULL,
    created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        DATETIME         NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_users_email (email),
    UNIQUE INDEX uk_users_phone (phone),
    INDEX idx_users_status_created_at (status, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Bảng roles: định nghĩa các vai trò trong hệ thống
CREATE TABLE roles
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50)     NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500)    NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_roles_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Bảng user_roles: quan hệ nhiều-nhiều giữa users và roles
CREATE TABLE user_roles
(
    user_id     BIGINT UNSIGNED NOT NULL,
    role_id     BIGINT UNSIGNED NOT NULL,
    assigned_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- null nếu được gán tự động (ví dụ: tự đăng ký)
    assigned_by BIGINT UNSIGNED NULL,

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- Dữ liệu mặc định: tạo 2 vai trò ADMIN và CLIENT
-- =====================================================
INSERT INTO roles (code, name, description)
VALUES ('ADMIN', 'Quản trị viên', 'Có toàn quyền quản lý hệ thống'),
       ('CLIENT', 'Khách hàng', 'Người dùng thông thường của website');
