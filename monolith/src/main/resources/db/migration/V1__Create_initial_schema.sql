-- ============================================================================
-- DATABASE: db_platform
-- 1. MONOLITH DOMAIN - Platform Services
-- Users, Products, Cart, Notifications, Settlement
-- ============================================================================
USE
db_platform;

-- Users table (Auth Service, User Service)
CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    username
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    email VARCHAR
(
    100
) NOT NULL UNIQUE,
    password VARCHAR
(
    255
) NOT NULL,
    phone_number VARCHAR
(
    20
),
    first_name VARCHAR
(
    50
),
    last_name VARCHAR
(
    50
),
    birth_date DATE,
    gender ENUM
(
    'M',
    'F',
    'OTHER'
),
    status ENUM
(
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'DELETED'
) NOT NULL DEFAULT 'ACTIVE',
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email
(
    email
),
    INDEX idx_users_username
(
    username
),
    INDEX idx_users_status
(
    status
),
    INDEX idx_users_created_at
(
    created_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- User addresses
CREATE TABLE IF NOT EXISTS user_addresses
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    user_id
    BIGINT
    NOT
    NULL,
    is_default
    BOOLEAN
    NOT
    NULL
    DEFAULT
    FALSE,
    recipient_name
    VARCHAR
(
    100
) NOT NULL,
    recipient_phone VARCHAR
(
    20
) NOT NULL,
    postal_code VARCHAR
(
    10
) NOT NULL,
    address_line1 VARCHAR
(
    255
) NOT NULL,
    address_line2 VARCHAR
(
    255
),
    city VARCHAR
(
    100
) NOT NULL,
    state VARCHAR
(
    100
) NOT NULL,
    country VARCHAR
(
    100
) NOT NULL DEFAULT 'KR',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
)
                                                           ON DELETE CASCADE,
    INDEX idx_user_addresses_user_id
(
    user_id
),
    INDEX idx_user_addresses_default
(
    user_id,
    is_default
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Product categories
CREATE TABLE IF NOT EXISTS product_categories
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    parent_id
    BIGINT
    NULL,
    name
    VARCHAR
(
    100
) NOT NULL,
    slug VARCHAR
(
    100
) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR
(
    500
),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    parent_id
) REFERENCES product_categories
(
    id
)
                                                           ON DELETE SET NULL,
    INDEX idx_categories_parent_id
(
    parent_id
),
    INDEX idx_categories_slug
(
    slug
),
    INDEX idx_categories_active
(
    is_active
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Products table (Product Service)
CREATE TABLE IF NOT EXISTS products
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    category_id
    BIGINT
    NOT
    NULL,
    sku
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    name VARCHAR
(
    255
) NOT NULL,
    description TEXT,
    short_description VARCHAR
(
    500
),
    brand VARCHAR
(
    100
),
    model VARCHAR
(
    100
),
    price DECIMAL
(
    10,
    2
) NOT NULL,
    compare_price DECIMAL
(
    10,
    2
), -- 할인 전 원가
    cost_price DECIMAL
(
    10,
    2
), -- 원가
    weight DECIMAL
(
    8,
    3
),
    product_attributes JSON, -- 상품별 속성
    status ENUM
(
    'DRAFT',
    'ACTIVE',
    'INACTIVE',
    'ARCHIVED'
) NOT NULL DEFAULT 'DRAFT',
    visibility ENUM
(
    'PUBLIC',
    'PRIVATE',
    'HIDDEN'
) NOT NULL DEFAULT 'PUBLIC',
    tax_class VARCHAR
(
    50
), -- 세금 분류 (면세, 과세 등)
    meta_title VARCHAR
(
    255
), -- SEO용 메타 제목
    meta_description VARCHAR
(
    500
), -- SEO용 메타 설명
    search_keywords TEXT,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE, -- 추천 상품 여부
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    category_id
) REFERENCES product_categories
(
    id
),
    INDEX idx_products_category_id
(
    category_id
),
    INDEX idx_products_sku
(
    sku
),
    INDEX idx_products_status
(
    status
),
    INDEX idx_products_visibility
(
    visibility
),
    INDEX idx_products_featured
(
    is_featured
),
    INDEX idx_products_price
(
    price
),
    INDEX idx_products_created_at
(
    created_at
),
    FULLTEXT idx_products_search
(
    name,
    description,
    search_keywords
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Product images
CREATE TABLE IF NOT EXISTS product_images
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    product_id
    BIGINT
    NOT
    NULL,
    image_url
    VARCHAR
(
    500
) NOT NULL,
    alt_text VARCHAR
(
    255
),
    sort_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
) ON DELETE CASCADE,
    INDEX idx_product_images_product_id
(
    product_id
),
    INDEX idx_product_images_primary
(
    product_id,
    is_primary
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Product variants (색상, 사이즈 등)
CREATE TABLE IF NOT EXISTS product_variants
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    product_id
    BIGINT
    NOT
    NULL,
    sku
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    name VARCHAR
(
    255
) NOT NULL,
    variant_attributes JSON, -- 변형별 속성 (색상, 사이즈 등)
    price DECIMAL
(
    10,
    2
), -- 변형별 가격 (없으면 기본 상품 가격 사용)
    compare_price DECIMAL
(
    10,
    2
),
    cost_price DECIMAL
(
    10,
    2
),
    weight DECIMAL
(
    8,
    3
),
    image_url VARCHAR
(
    500
),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
)
                                                           ON DELETE CASCADE,
    INDEX idx_product_variants_product_id
(
    product_id
),
    INDEX idx_product_variants_sku
(
    sku
),
    INDEX idx_product_variants_active
(
    is_active
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Product inventory (재고 관리)
CREATE TABLE IF NOT EXISTS product_inventories
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    product_id
    BIGINT
    NOT
    NULL,
    product_variant_id
    BIGINT
    NULL,
    available_quantity
    INT
    NOT
    NULL
    DEFAULT
    0,     -- 판매 가능 재고
    reserved_quantity
    INT
    NOT
    NULL
    DEFAULT
    0,     -- 예약된 재고 (주문 대기)
    total_quantity
    INT
    NOT
    NULL
    DEFAULT
    0,     -- 전체 재고
    low_stock_threshold
    INT
    NOT
    NULL
    DEFAULT
    10,    -- 재고 부족 임계값
    is_tracking_enabled
    BOOLEAN
    NOT
    NULL
    DEFAULT
    TRUE,  -- 재고 추적 여부
    is_backorder_allowed
    BOOLEAN
    NOT
    NULL
    DEFAULT
    FALSE, -- 품절 시 주문 허용 여부
    last_updated_at
    DATETIME
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP
    ON
    UPDATE
    CURRENT_TIMESTAMP,
    created_at
    DATETIME
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,

    FOREIGN
    KEY
(
    product_id
) REFERENCES products
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    product_variant_id
) REFERENCES product_variants
(
    id
)
  ON DELETE CASCADE,
    UNIQUE KEY uk_inventory_product_variant
(
    product_id,
    product_variant_id
),
    INDEX idx_inventory_product_id
(
    product_id
),
    INDEX idx_inventory_available_qty
(
    available_quantity
),
    INDEX idx_inventory_low_stock
(
    low_stock_threshold
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Shopping cart (Cart Service)
CREATE TABLE IF NOT EXISTS shopping_carts
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    user_id
    BIGINT
    NOT
    NULL,
    session_id
    VARCHAR
(
    255
),
    status ENUM
(
    'ACTIVE',
    'ABANDONED',
    'CONVERTED'
) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
)
                                                           ON DELETE CASCADE,
    INDEX idx_shopping_carts_user_id
(
    user_id
),
    INDEX idx_shopping_carts_session_id
(
    session_id
),
    INDEX idx_shopping_carts_status
(
    status
),
    INDEX idx_shopping_carts_updated_at
(
    updated_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Shopping cart items
CREATE TABLE IF NOT EXISTS shopping_cart_items
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    cart_id
    BIGINT
    NOT
    NULL,
    product_id
    BIGINT
    NOT
    NULL,
    product_variant_id
    BIGINT
    NULL,
    quantity
    INT
    NOT
    NULL
    DEFAULT
    1,
    unit_price
    DECIMAL
(
    10,
    2
) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    cart_id
) REFERENCES shopping_carts
(
    id
)
                                                           ON DELETE CASCADE,
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
)
                                                           ON DELETE CASCADE,
    FOREIGN KEY
(
    product_variant_id
) REFERENCES product_variants
(
    id
)
                                                           ON DELETE CASCADE,
    INDEX idx_cart_items_cart_id
(
    cart_id
),
    INDEX idx_cart_items_product_id
(
    product_id
),
    UNIQUE KEY uk_cart_items_product
(
    cart_id,
    product_id,
    product_variant_id
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Notifications
CREATE TABLE IF NOT EXISTS notifications
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    recipient_type
    ENUM
(
    'USER',
    'ADMIN',
    'DEVELOPER'
) NOT NULL DEFAULT 'USER',
    recipient_id BIGINT NULL, -- user_id (USER인 경우), NULL(ADMIN/DEVELOPER인 경우)
    recipient_emails JSON, -- ADMIN/DEVELOPER인 경우 이메일 리스트
    type ENUM
(
    'EMAIL',
    'SMS',
    'PUSH',
    'IN_APP',
    'SLACK',
    'WEBHOOK'
) NOT NULL,
    category ENUM
(
    'ORDER',
    'PAYMENT',
    'SHIPPING',
    'PROMOTION',
    'SYSTEM',
    'DLT',
    'ERROR',
    'WARNING'
) NOT NULL,
    title VARCHAR
(
    255
) NOT NULL,
    content TEXT NOT NULL,
    status ENUM
(
    'PENDING',
    'SENT',
    'DELIVERED',
    'FAILED',
    'read'
) NOT NULL DEFAULT 'PENDING',
    priority ENUM
(
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT',
    'CRITICAL'
) NOT NULL DEFAULT 'MEDIUM',
    scheduled_at DATETIME NULL,
    sent_at DATETIME NULL,
    delivered_at DATETIME NULL,
    read_at DATETIME NULL,
    reference_type VARCHAR
(
    50
), -- 'order', 'payment', 'dlt_message' 등
    reference_id BIGINT,
    metadata JSON, -- DLT 상세 정보
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    recipient_id
) REFERENCES users
(
    id
)
                                                           ON DELETE CASCADE,
    INDEX idx_notifications_recipient
(
    recipient_type,
    recipient_id
),
    INDEX idx_notifications_type
(
    type
),
    INDEX idx_notifications_category
(
    category
),
    INDEX idx_notifications_status
(
    status
),
    INDEX idx_notifications_priority
(
    priority
),
    INDEX idx_notifications_scheduled_at
(
    scheduled_at
),
    INDEX idx_notifications_reference
(
    reference_type,
    reference_id
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- SETTLEMENT DOMAIN - Settlement Service (in db_platform)

-- Payment Settlement Events (결제 정산 이벤트 로그) - PaymentService 이벤트 기반 적재
CREATE TABLE IF NOT EXISTS payment_settlement_events
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
    payment_id BIGINT NOT NULL, -- Cross-domain reference (from db_payment)
    order_id BIGINT NOT NULL, -- Cross-domain reference (from db_order)
    user_id BIGINT NOT NULL, -- Cross-domain reference (from db_platform.users)

-- Payment event details
    payment_amount DECIMAL
(
    10,
    2
) NOT NULL,
    payment_currency VARCHAR
(
    3
) NOT NULL DEFAULT 'KRW',
    payment_method_type ENUM
(
    'CREDIT_CARD',
    'DEBIT_CARD',
    'BANK_TRANSFER',
    'DIGITAL_WALLET',
    'CRYPTOCURRENCY',
    'VIRTUAL_ACCOUNT'
) NOT NULL,
    gateway_provider ENUM
(
    'TOSS',
    'NICE',
    'KCP',
    'PAYPAL',
    'STRIPE'
) NOT NULL,

    -- Settlement calculation
    gross_amount DECIMAL
(
    10,
    2
) NOT NULL,
    gateway_fee_rate DECIMAL
(
    5,
    4
) NOT NULL DEFAULT 0.0300, -- 3% 기본값
    gateway_fee_amount DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    platform_fee_rate DECIMAL
(
    5,
    4
) NOT NULL DEFAULT 0.0100, -- 1% 기본값
    platform_fee_amount DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL
(
    10,
    2
) NOT NULL,

    -- Event metadata
    event_type ENUM
(
    'PAYMENT_COMPLETED',
    'PAYMENT_REFUNDED',
    'PAYMENT_PARTIALLY_REFUNDED'
) NOT NULL,
    settlement_target_date DATE NOT NULL, -- 정산 대상 날짜 (결제일 기준)
    processed_at DATETIME NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement_events_payment_id
(
    payment_id
),
    INDEX idx_settlement_events_order_id
(
    order_id
),
    INDEX idx_settlement_events_user_id
(
    user_id
),
    INDEX idx_settlement_events_target_date
(
    settlement_target_date
),
    INDEX idx_settlement_events_event_type
(
    event_type
),
    INDEX idx_settlement_events_processed_at
(
    processed_at
),
    INDEX idx_settlement_events_gateway
(
    gateway_provider
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Daily Settlement Aggregation
CREATE TABLE IF NOT EXISTS daily_settlements
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    settlement_date
    DATE
    NOT
    NULL,

    -- Aggregation metadata
    total_payment_count
    INT
    NOT
    NULL
    DEFAULT
    0,
    total_refund_count
    INT
    NOT
    NULL
    DEFAULT
    0,

    -- Payment method breakdown
    credit_card_amount
    DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    credit_card_count INT NOT NULL DEFAULT 0,
    debit_card_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    debit_card_count INT NOT NULL DEFAULT 0,
    bank_transfer_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    bank_transfer_count INT NOT NULL DEFAULT 0,
    digital_wallet_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    digital_wallet_count INT NOT NULL DEFAULT 0,

    -- Gateway breakdown
    toss_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    toss_count INT NOT NULL DEFAULT 0,
    nice_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    nice_count INT NOT NULL DEFAULT 0,
    kcp_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    kcp_count INT NOT NULL DEFAULT 0,

    -- Financial summary
    total_gross_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    total_gateway_fee DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    total_platform_fee DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    total_tax_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,
    total_net_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,

    -- Processing status
    status ENUM
(
    'CALCULATING',
    'COMPLETED',
    'RECONCILED'
) NOT NULL DEFAULT 'CALCULATING',
    calculated_at DATETIME NULL,
    reconciled_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_settlements_date
(
    settlement_date
),
    INDEX idx_daily_settlements_status
(
    status
),
    INDEX idx_daily_settlements_calculated_at
(
    calculated_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Weekly Settlement Aggregation
CREATE TABLE IF NOT EXISTS weekly_settlements
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    year_week
    VARCHAR
(
    7
) NOT NULL, -- 'YYYY-WW' format (e.g., '2024-52')
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,

    -- Financial summary
    total_gross_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_gateway_fee DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_platform_fee DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_tax_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_net_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,

    total_payment_count INT NOT NULL DEFAULT 0,
    total_refund_count INT NOT NULL DEFAULT 0,

    -- Top payment methods (JSON for flexibility)
    payment_method_breakdown JSON,
    gateway_breakdown JSON,

    -- Processing status
    status ENUM
(
    'CALCULATING',
    'COMPLETED',
    'RECONCILED'
) NOT NULL DEFAULT 'CALCULATING',
    calculated_at DATETIME NULL,
    reconciled_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_weekly_settlements_year_week
(
    year_week
),
    INDEX idx_weekly_settlements_week_start
(
    week_start_date
),
    INDEX idx_weekly_settlements_status
(
    status
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Monthly Settlement Aggregation
CREATE TABLE IF NOT EXISTS monthly_settlements
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    settlement_year_month
    VARCHAR
(
    7
) NOT NULL, -- 'YYYY-MM' format (e.g., '2024-12')
    month_start_date DATE NOT NULL,
    month_end_date DATE NOT NULL,

    -- Financial summary
    total_gross_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_gateway_fee DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_platform_fee DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_tax_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_net_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,

    total_payment_count INT NOT NULL DEFAULT 0,
    total_refund_count INT NOT NULL DEFAULT 0,

    -- Detailed breakdown
    payment_method_breakdown JSON,
    gateway_breakdown JSON,
    daily_trend JSON, -- 일별 트렌드 데이터

-- Business insights
    avg_transaction_amount DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    peak_day DATE NULL,
    peak_day_amount DECIMAL
(
    12,
    2
) NOT NULL DEFAULT 0.00,

    -- Processing status
    status ENUM
(
    'CALCULATING',
    'COMPLETED',
    'RECONCILED',
    'FINALIZED'
) NOT NULL DEFAULT 'CALCULATING',
    calculated_at DATETIME NULL,
    reconciled_at DATETIME NULL,
    finalized_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_monthly_settlements_year_month
(
    settlement_year_month
),
    INDEX idx_monthly_settlements_month_start
(
    month_start_date
),
    INDEX idx_monthly_settlements_status
(
    status
),
    INDEX idx_monthly_settlements_finalized_at
(
    finalized_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Settlement Gateway Batches (PG사 정산 배치 정보)
CREATE TABLE IF NOT EXISTS settlement_gateway_batches
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    batch_id
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    gateway_provider ENUM
(
    'TOSS',
    'NICE',
    'KCP',
    'PAYPAL',
    'STRIPE'
) NOT NULL,
    settlement_date DATE NOT NULL,

    -- Batch information
    total_payment_count INT NOT NULL DEFAULT 0,
    total_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    total_fee DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,
    net_settlement_amount DECIMAL
(
    15,
    2
) NOT NULL DEFAULT 0.00,

    -- Gateway settlement details
    gateway_settlement_id VARCHAR
(
    255
),
    gateway_response JSON,
    settlement_account VARCHAR
(
    100
),

    -- Processing status
    status ENUM
(
    'PENDING',
    'PROCESSING',
    'COMPLETED',
    'FAILED',
    'RECONCILED'
) NOT NULL DEFAULT 'PENDING',
    processed_at DATETIME NULL,
    reconciled_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_gateway_batches_provider_date
(
    gateway_provider,
    settlement_date
),
    INDEX idx_gateway_batches_status
(
    status
),
    INDEX idx_gateway_batches_processed_at
(
    processed_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Event sourcing table for db_platform
CREATE TABLE IF NOT EXISTS event_store
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
    occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_store_aggregate
(
    aggregate_type,
    aggregate_id
),
    INDEX idx_event_store_type
(
    event_type
),
    INDEX idx_event_store_occurred_at
(
    occurred_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Product inventory snapshots for materialized views
CREATE TABLE IF NOT EXISTS product_inventory_snapshots
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    product_id
    BIGINT
    NOT
    NULL,
    product_variant_id
    BIGINT
    NULL,
    available_quantity
    INT
    NOT
    NULL
    DEFAULT
    0,
    reserved_quantity
    INT
    NOT
    NULL
    DEFAULT
    0,
    total_quantity
    INT
    NOT
    NULL
    DEFAULT
    0,
    snapshot_timestamp
    DATETIME
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    snapshot_reason
    ENUM
(
    'SCHEDULED',
    'MANUAL',
    'DISASTER_RECOVERY'
) NOT NULL DEFAULT 'SCHEDULED',
    INDEX idx_inventory_snapshots_product_id
(
    product_id
),
    INDEX idx_inventory_snapshots_variant_id
(
    product_variant_id
),
    INDEX idx_inventory_snapshots_timestamp
(
    snapshot_timestamp
),
    INDEX idx_inventory_snapshots_reason
(
    snapshot_reason
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Performance optimizations
ALTER TABLE products
    ADD INDEX idx_products_category_status (category_id, status);
ALTER TABLE notifications
    ADD INDEX idx_notifications_recipient_status (recipient_type, status);
ALTER TABLE payment_settlement_events
    ADD INDEX idx_settlement_events_date_provider (settlement_target_date, gateway_provider);
ALTER TABLE daily_settlements
    ADD INDEX idx_daily_settlements_date_status (settlement_date, status);
ALTER TABLE weekly_settlements
    ADD INDEX idx_weekly_settlements_dates (week_start_date, week_end_date);
ALTER TABLE monthly_settlements
    ADD INDEX idx_monthly_settlements_dates (month_start_date, month_end_date);
