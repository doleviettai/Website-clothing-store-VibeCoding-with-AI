-- =====================================================
-- V19: Tạo bảng orders, order_items và payments cho Quản lý Đơn hàng & Giao dịch
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Bảng orders
CREATE TABLE IF NOT EXISTS orders
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    order_code     VARCHAR(50)    NOT NULL,
    user_id        BIGINT UNSIGNED NOT NULL,
    customer_name  VARCHAR(100)   NOT NULL,
    phone          VARCHAR(20)    NOT NULL,
    email          VARCHAR(191)   NULL,
    province       VARCHAR(100)   NOT NULL,
    district       VARCHAR(100)   NULL,
    ward           VARCHAR(100)   NOT NULL,
    street_address VARCHAR(255)   NOT NULL,
    note           TEXT           NULL,
    subtotal       DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    shipping_fee   DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_amount   DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50)    NOT NULL DEFAULT 'COD',
    payment_status VARCHAR(30)    NOT NULL DEFAULT 'UNPAID',
    status         VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_code (order_code),
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_created_at (created_at),

    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Bảng order_items
CREATE TABLE IF NOT EXISTS order_items
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    order_id          BIGINT         NOT NULL,
    product_id        BIGINT         NOT NULL,
    product_name      VARCHAR(255)   NOT NULL,
    product_thumbnail VARCHAR(500)   NULL,
    quantity          INT            NOT NULL DEFAULT 1,
    price             DECIMAL(15, 2) NOT NULL,
    size              VARCHAR(50)    NULL,
    color             VARCHAR(50)    NULL,
    total_price       DECIMAL(15, 2) NOT NULL,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id),

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Bảng payments (Lịch sử giao dịch thanh toán)
CREATE TABLE IF NOT EXISTS payments
(
    id                     BIGINT         NOT NULL AUTO_INCREMENT,
    order_id               BIGINT         NOT NULL,
    transaction_code       VARCHAR(100)   NOT NULL,
    gateway_transaction_no VARCHAR(100)   NULL,
    payment_gateway        VARCHAR(50)    NOT NULL DEFAULT 'COD',
    payment_method         VARCHAR(50)    NOT NULL DEFAULT 'COD',
    amount                 DECIMAL(15, 2) NOT NULL,
    currency               VARCHAR(10)    NOT NULL DEFAULT 'VND',
    status                 VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    payment_info           TEXT           NULL,
    paid_at                DATETIME       NULL,
    created_at             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_transaction_code (transaction_code),
    INDEX idx_payments_order (order_id),
    INDEX idx_payments_gateway (payment_gateway),
    INDEX idx_payments_status (status),
    INDEX idx_payments_created_at (created_at),

    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
