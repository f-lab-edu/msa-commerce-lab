-- ============================================================================
-- DATABASE: db_order
-- 2. ORDER DOMAIN - Order Orchestrator Service
-- ============================================================================

-- Switch to order database context
USE db_order;

-- Orders table (Order Orchestrator Service)
CREATE TABLE IF NOT EXISTS orders
(
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number           VARCHAR(50)                                                                          NOT NULL UNIQUE,
    user_id                BIGINT                                                                               NOT NULL, -- Cross-domain reference (from db_platform.users)

-- Order details
    total_amount           DECIMAL(10, 2)                                                                       NOT NULL,
    subtotal_amount        DECIMAL(10, 2)                                                                       NOT NULL,
    discount_amount        DECIMAL(10, 2)                                                                       NOT NULL DEFAULT 0.00,
    tax_amount             DECIMAL(10, 2)                                                                       NOT NULL DEFAULT 0.00,
    shipping_amount        DECIMAL(10, 2)                                                                       NOT NULL DEFAULT 0.00,
    currency               VARCHAR(3)                                                                           NOT NULL DEFAULT 'KRW',

    -- Status tracking
    status                 ENUM ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')   NOT NULL DEFAULT 'PENDING',
    payment_status         ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')     NOT NULL DEFAULT 'PENDING',
    fulfillment_status     ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED')    NOT NULL DEFAULT 'PENDING',

    -- Shipping information
    shipping_method        ENUM ('STANDARD', 'EXPRESS', 'OVERNIGHT', 'PICKUP')                                  NOT NULL DEFAULT 'STANDARD',
    tracking_number        VARCHAR(100),
    carrier                VARCHAR(100),
    estimated_delivery_at  DATETIME                                                                             NULL,

    -- Billing information
    billing_address        JSON                                                                                 NOT NULL,
    shipping_address       JSON                                                                                 NOT NULL,
    customer_notes         TEXT,

    -- Important timestamps
    confirmed_at           DATETIME                                                                             NULL,
    shipped_at             DATETIME                                                                             NULL,
    delivered_at           DATETIME                                                                             NULL,
    cancelled_at           DATETIME                                                                             NULL,

    created_at             DATETIME                                                                             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME                                                                             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Note: No foreign key constraints for cross-domain references
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_order_number (order_number),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_fulfillment_status (fulfillment_status),
    INDEX idx_orders_created_at (created_at),
    INDEX idx_orders_total_amount (total_amount),
    INDEX idx_orders_tracking_number (tracking_number)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Order items
CREATE TABLE IF NOT EXISTS order_items
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id             BIGINT                                                              NOT NULL,
    product_id           BIGINT                                                              NOT NULL, -- Cross-domain reference (from db_platform.products)
    product_variant_id   BIGINT                                                              NULL,     -- Cross-domain reference (from db_platform.product_variants)

    -- Product snapshot at time of order
    product_sku          VARCHAR(100)                                                        NOT NULL,
    product_name         VARCHAR(255)                                                        NOT NULL,
    product_variant_name VARCHAR(255),
    product_image_url    VARCHAR(500),

    -- Pricing information
    unit_price           DECIMAL(10, 2)                                                      NOT NULL,
    quantity             INT                                                                 NOT NULL DEFAULT 1,
    total_price          DECIMAL(10, 2)                                                      NOT NULL,

    -- Fulfillment tracking
    fulfillment_status   ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    shipped_quantity     INT                                                                 NOT NULL DEFAULT 0,
    cancelled_quantity   INT                                                                 NOT NULL DEFAULT 0,

    created_at           DATETIME                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    -- Note: No foreign key constraints for cross-domain references (product_id, product_variant_id)
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),
    INDEX idx_order_items_fulfillment_status (fulfillment_status)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Order status history (for tracking state changes)
CREATE TABLE IF NOT EXISTS order_status_history
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id           BIGINT                                   NOT NULL,
    status_type        ENUM ('ORDER', 'PAYMENT', 'FULFILLMENT') NOT NULL,
    from_status        VARCHAR(50),
    to_status          VARCHAR(50)                              NOT NULL,
    reason             VARCHAR(255),
    notes              TEXT,
    changed_by_user_id BIGINT, -- Cross-domain reference (from db_platform.users)
    created_at         DATETIME                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    -- Note: No foreign key constraint for cross-domain reference (changed_by_user_id)
    INDEX idx_order_status_history_order_id (order_id),
    INDEX idx_order_status_history_created_at (created_at)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- db_order 최적화
ALTER TABLE db_order.orders
    ADD INDEX idx_orders_user_status (user_id, status);
ALTER TABLE db_order.orders
    ADD INDEX idx_orders_payment_status_created (payment_status, created_at);
ALTER TABLE db_order.order_items
    ADD INDEX idx_order_items_product_status (product_id, fulfillment_status);