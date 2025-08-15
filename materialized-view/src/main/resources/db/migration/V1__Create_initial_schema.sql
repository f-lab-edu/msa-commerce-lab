-- ============================================================================
-- Materialized View Service Initial Schema - Analytics Domain (materialized_view_db)
-- Based on V0__Initial_schema.sql from centralized migration
-- Database: materialized_view_db (localhost:3306)
-- ============================================================================

-- Switch to materialized view database
USE
materialized_view_db;

-- Product sales summary (materialized view)
CREATE TABLE IF NOT EXISTS product_sales_summary
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    product_id
    BIGINT
    NOT
    NULL, -- Cross-domain reference (from db_platform.products)
    product_sku
    VARCHAR
(
    100
) NOT NULL,
    product_name VARCHAR
(
    255
) NOT NULL,

    -- Sales metrics
    total_quantity_sold INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    average_selling_price DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    total_orders INT NOT NULL DEFAULT 0,
    unique_customers INT NOT NULL DEFAULT 0,

    -- Time-based metrics
    last_sale_date DATE NULL,
    first_sale_date DATE NULL,

    -- Summary period
    summary_period ENUM
(
    'DAILY',
    'WEEKLY',
    'MONTHLY',
    'YEARLY',
    'LIFETIME'
) NOT NULL,
    period_start_date DATE NULL,
    period_end_date DATE NULL,

    last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_sales_product_id
(
    product_id
),
    INDEX idx_product_sales_sku
(
    product_sku
),
    INDEX idx_product_sales_period
(
    summary_period,
    period_start_date
),
    INDEX idx_product_sales_revenue
(
    total_revenue
),
    INDEX idx_product_sales_quantity
(
    total_quantity_sold
),
    UNIQUE KEY uk_product_sales_period
(
    product_id,
    summary_period,
    period_start_date
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- User order summary (materialized view)
CREATE TABLE IF NOT EXISTS user_order_summary
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    user_id
    BIGINT
    NOT
    NULL, -- Cross-domain reference (from db_platform.users)

    -- Order metrics
    total_orders
    INT
    NOT
    NULL
    DEFAULT
    0,
    completed_orders
    INT
    NOT
    NULL
    DEFAULT
    0,
    cancelled_orders
    INT
    NOT
    NULL
    DEFAULT
    0,
    pending_orders
    INT
    NOT
    NULL
    DEFAULT
    0,

    -- Financial metrics
    total_spent
    DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    average_order_value DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    lifetime_value DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,

    -- Behavioral metrics
    first_order_date DATE NULL,
    last_order_date DATE NULL,
    days_between_orders INT NULL, -- 평균 주문 간격
    favorite_category_id BIGINT NULL, -- 가장 많이 구매한 카테고리

-- Summary period
    summary_period ENUM
(
    'MONTHLY',
    'YEARLY',
    'LIFETIME'
) NOT NULL,
    period_start_date DATE NULL,
    period_end_date DATE NULL,

    last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_order_user_id
(
    user_id
),
    INDEX idx_user_order_period
(
    summary_period,
    period_start_date
),
    INDEX idx_user_order_total_spent
(
    total_spent
),
    INDEX idx_user_order_lifetime_value
(
    lifetime_value
),
    UNIQUE KEY uk_user_order_period
(
    user_id,
    summary_period,
    period_start_date
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Daily business metrics (materialized view)
CREATE TABLE IF NOT EXISTS daily_business_metrics
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    business_date
    DATE
    NOT
    NULL,

    -- Order metrics
    total_orders
    INT
    NOT
    NULL
    DEFAULT
    0,
    completed_orders
    INT
    NOT
    NULL
    DEFAULT
    0,
    cancelled_orders
    INT
    NOT
    NULL
    DEFAULT
    0,
    average_order_value
    DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,

    -- Revenue metrics
    total_revenue DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    gross_profit DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    net_profit DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,

    -- Customer metrics
    new_customers INT NOT NULL DEFAULT 0,
    returning_customers INT NOT NULL DEFAULT 0,
    unique_customers INT NOT NULL DEFAULT 0,

    -- Product metrics
    products_sold INT NOT NULL DEFAULT 0,
    unique_products_sold INT NOT NULL DEFAULT 0,
    top_selling_product_id BIGINT NULL, -- Cross-domain reference

-- Operational metrics
    inventory_turnover DECIMAL
(
    8,
    4
) NULL,
    conversion_rate DECIMAL
(
    5,
    2
) NULL, -- 방문자 대비 구매 전환율

    last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_business_date
(
    business_date
),
    INDEX idx_daily_business_total_revenue
(
    total_revenue
),
    INDEX idx_daily_business_total_orders
(
    total_orders
),
    INDEX idx_daily_business_conversion_rate
(
    conversion_rate
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Event sourcing table for all domains (centralized logging)
CREATE TABLE IF NOT EXISTS domain_events_log
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    event_id
    VARCHAR
(
    36
) NOT NULL UNIQUE,
    domain ENUM
(
    'PLATFORM',
    'ORDER',
    'PAYMENT',
    'MATERIALIZED_VIEW'
) NOT NULL,
    event_type VARCHAR
(
    100
) NOT NULL,
    aggregate_type VARCHAR
(
    100
) NOT NULL,
    aggregate_id VARCHAR
(
    100
) NOT NULL,
    event_version INT NOT NULL DEFAULT 1,
    event_data JSON NOT NULL,
    metadata JSON,
    correlation_id VARCHAR
(
    36
), -- 연관 이벤트 그룹화
    causation_id VARCHAR
(
    36
), -- 이벤트 원인 추적
    occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME NULL,
    INDEX idx_domain_events_domain
(
    domain
),
    INDEX idx_domain_events_aggregate
(
    aggregate_type,
    aggregate_id
),
    INDEX idx_domain_events_type
(
    event_type
),
    INDEX idx_domain_events_occurred_at
(
    occurred_at
),
    INDEX idx_domain_events_correlation
(
    correlation_id
),
    INDEX idx_domain_events_causation
(
    causation_id
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Order summary materialized view (legacy compatibility)
CREATE TABLE IF NOT EXISTS order_summary_view
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    order_id
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_name VARCHAR
(
    100
),
    user_email VARCHAR
(
    100
),
    order_number VARCHAR
(
    50
) NOT NULL,
    order_status VARCHAR
(
    30
) NOT NULL,
    payment_status VARCHAR
(
    30
),
    total_amount DECIMAL
(
    10,
    2
) NOT NULL,
    total_items INT DEFAULT 0,
    shipping_address TEXT,
    billing_address TEXT,
    order_created_at TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_summary_order_id
(
    order_id
),
    INDEX idx_order_summary_user_id
(
    user_id
),
    INDEX idx_order_summary_order_number
(
    order_number
),
    INDEX idx_order_summary_order_status
(
    order_status
),
    INDEX idx_order_summary_payment_status
(
    payment_status
),
    INDEX idx_order_summary_order_created_at
(
    order_created_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Product analytics view (legacy compatibility)
CREATE TABLE IF NOT EXISTS product_analytics_view
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    product_id
    BIGINT
    NOT
    NULL
    UNIQUE,
    product_name
    VARCHAR
(
    200
) NOT NULL,
    category_name VARCHAR
(
    100
),
    current_price DECIMAL
(
    10,
    2
),
    total_orders INT DEFAULT 0,
    total_quantity_sold INT DEFAULT 0,
    total_revenue DECIMAL
(
    12,
    2
) DEFAULT 0,
    average_order_quantity DECIMAL
(
    5,
    2
) DEFAULT 0,
    last_ordered_at TIMESTAMP NULL,
    current_stock INT DEFAULT 0,
    stock_status VARCHAR
(
    20
), -- IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_analytics_product_id
(
    product_id
),
    INDEX idx_product_analytics_product_name
(
    product_name
),
    INDEX idx_product_analytics_category_name
(
    category_name
),
    INDEX idx_product_analytics_total_orders
(
    total_orders
),
    INDEX idx_product_analytics_total_revenue
(
    total_revenue
),
    INDEX idx_product_analytics_stock_status
(
    stock_status
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- User activity view (legacy compatibility)
CREATE TABLE IF NOT EXISTS user_activity_view
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    user_id
    BIGINT
    NOT
    NULL
    UNIQUE,
    username
    VARCHAR
(
    50
) NOT NULL,
    email VARCHAR
(
    100
) NOT NULL,
    full_name VARCHAR
(
    100
),
    total_orders INT DEFAULT 0,
    total_spent DECIMAL
(
    12,
    2
) DEFAULT 0,
    average_order_value DECIMAL
(
    10,
    2
) DEFAULT 0,
    last_order_date TIMESTAMP NULL,
    first_order_date TIMESTAMP NULL,
    favorite_category VARCHAR
(
    100
),
    account_status VARCHAR
(
    20
), -- ACTIVE, INACTIVE, SUSPENDED
    last_login_at TIMESTAMP NULL,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_activity_user_id
(
    user_id
),
    INDEX idx_user_activity_username
(
    username
),
    INDEX idx_user_activity_email
(
    email
),
    INDEX idx_user_activity_total_orders
(
    total_orders
),
    INDEX idx_user_activity_total_spent
(
    total_spent
),
    INDEX idx_user_activity_account_status
(
    account_status
),
    INDEX idx_user_activity_last_order_date
(
    last_order_date
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Event processing log
CREATE TABLE IF NOT EXISTS event_processing_log
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    event_id
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    event_type VARCHAR
(
    50
) NOT NULL,
    source_service VARCHAR
(
    30
) NOT NULL,
    event_data JSON,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processing_status VARCHAR
(
    20
) DEFAULT 'SUCCESS', -- SUCCESS, FAILED, RETRY
    error_message TEXT,
    retry_count INT DEFAULT 0,
    INDEX idx_event_processing_event_id
(
    event_id
),
    INDEX idx_event_processing_event_type
(
    event_type
),
    INDEX idx_event_processing_source_service
(
    source_service
),
    INDEX idx_event_processing_processed_at
(
    processed_at
),
    INDEX idx_event_processing_processing_status
(
    processing_status
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- View refresh status tracking
CREATE TABLE IF NOT EXISTS view_refresh_status
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    view_name
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    last_refresh_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    refresh_status VARCHAR
(
    20
) DEFAULT 'SUCCESS', -- SUCCESS, FAILED, IN_PROGRESS
    records_processed INT DEFAULT 0,
    error_message TEXT,
    refresh_duration_ms BIGINT,
    next_scheduled_refresh TIMESTAMP NULL,
    INDEX idx_view_refresh_view_name
(
    view_name
),
    INDEX idx_view_refresh_last_refresh_at
(
    last_refresh_at
),
    INDEX idx_view_refresh_refresh_status
(
    refresh_status
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Performance optimizations
ALTER TABLE product_sales_summary
    ADD INDEX idx_product_sales_updated (last_updated_at);
ALTER TABLE user_order_summary
    ADD INDEX idx_user_order_updated (last_updated_at);
ALTER TABLE daily_business_metrics
    ADD INDEX idx_daily_business_updated (last_updated_at);