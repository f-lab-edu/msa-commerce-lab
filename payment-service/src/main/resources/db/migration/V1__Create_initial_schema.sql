-- ============================================================================
-- DATABASE: db_payment
-- 3. PAYMENT DOMAIN - Payment Service
-- ============================================================================

-- Switch to payment database context
USE db_payment;

-- Payments
CREATE TABLE IF NOT EXISTS payments
(
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id               BIGINT                                                                         NOT NULL, -- Cross-domain reference (from db_order.orders)

    -- Payment gateway information
    gateway_provider       ENUM ('TOSS', 'NICE', 'KCP', 'PAYPAL', 'STRIPE')                               NOT NULL,
    gateway_transaction_id VARCHAR(255),
    gateway_payment_key    VARCHAR(255),

    -- Payment details
    amount                 DECIMAL(10, 2)                                                                 NOT NULL,
    currency               VARCHAR(3)                                                                     NOT NULL DEFAULT 'KRW',
    payment_method_type    ENUM ('CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'DIGITAL_WALLET', 'CRYPTOCURRENCY', 'VIRTUAL_ACCOUNT') NOT NULL,
    payment_method_details JSON,                                                                           -- 카드 마지막 4자리, 은행명 등

-- Status and processing
    status                 ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    failure_reason         VARCHAR(255),
    gateway_response       JSON,                                                                           -- PG사 응답 전체

-- Timestamps
    processed_at           DATETIME                                                                       NULL,
    completed_at           DATETIME                                                                       NULL,
    cancelled_at           DATETIME                                                                       NULL,
    failed_at              DATETIME                                                                       NULL,

    created_at             DATETIME                                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME                                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Note: No foreign key constraints for cross-domain references
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_gateway_transaction_id (gateway_transaction_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_gateway_provider (gateway_provider),
    INDEX idx_payments_payment_method_type (payment_method_type),
    INDEX idx_payments_created_at (created_at),
    INDEX idx_payments_amount (amount)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Payment refunds
CREATE TABLE IF NOT EXISTS payment_refunds
(
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id             BIGINT                                             NOT NULL,
    refund_number          VARCHAR(50)                                        NOT NULL UNIQUE,

    -- Refund details
    amount                 DECIMAL(10, 2)                                     NOT NULL,
    currency               VARCHAR(3)                                         NOT NULL DEFAULT 'KRW',
    reason                 ENUM ('CUSTOMER_REQUEST', 'DEFECTIVE_PRODUCT', 'ORDER_CANCELLATION', 'FRAUD', 'OTHER') NOT NULL,
    reason_description     TEXT,

    -- Gateway information
    gateway_refund_id      VARCHAR(255),
    gateway_response       JSON,

    -- Status and processing
    status                 ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    failure_reason         VARCHAR(255),

    -- Important timestamps
    processed_at           DATETIME                                           NULL,
    completed_at           DATETIME                                           NULL,
    failed_at              DATETIME                                           NULL,

    created_at             DATETIME                                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE,
    INDEX idx_payment_refunds_payment_id (payment_id),
    INDEX idx_payment_refunds_refund_number (refund_number),
    INDEX idx_payment_refunds_status (status),
    INDEX idx_payment_refunds_created_at (created_at)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- db_payment 최적화
ALTER TABLE db_payment.payments
    ADD INDEX idx_payments_status_created (status, created_at);
ALTER TABLE db_payment.payment_methods
    ADD INDEX idx_payment_methods_user_type (user_id, type);