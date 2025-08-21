-- ============================================================================
-- MSA Commerce Lab - 통합 MySQL 데이터베이스 스키마
-- Version: V0 (Unified Schema for All Microservices)
-- Date: 2025-01-21
-- Description: 마이크로서비스 아키텍처를 위한 통합 MySQL 9.0 스키마
-- Services: Platform, Order, Payment, Materialized View
-- ============================================================================

-- MySQL 설정
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';
SET innodb_file_per_table = ON;
SET innodb_default_row_format = dynamic;

-- 데이터베이스 문자셋 설정
ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================================
-- 1. 플랫폼 도메인 - 사용자 관리 (Auth Service, User Service)
-- ============================================================================

-- 사용자 계정
CREATE TABLE users (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_uuid           CHAR(36) UNIQUE NOT NULL DEFAULT (UUID()),
    username            VARCHAR(50) UNIQUE NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20),
    date_of_birth       DATE,
    gender              ENUM('MALE', 'FEMALE', 'OTHER'),
    status              ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    email_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    profile_image_url   VARCHAR(500),
    last_login_at       TIMESTAMP NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 주소
CREATE TABLE user_addresses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    address_type    ENUM('HOME', 'WORK', 'BILLING', 'SHIPPING', 'OTHER') NOT NULL DEFAULT 'HOME',
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_name  VARCHAR(100) NOT NULL,
    phone_number    VARCHAR(20),
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    state_province  VARCHAR(100),
    postal_code     VARCHAR(20) NOT NULL,
    country         VARCHAR(100) NOT NULL DEFAULT 'Korea',
    latitude        DECIMAL(10, 8),
    longitude       DECIMAL(11, 8),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 2. 플랫폼 도메인 - 상품 관리 (Product Service) - 단순화
-- ============================================================================

-- 상품 카테고리 (단순화)
CREATE TABLE product_categories (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id           BIGINT NULL,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    slug                VARCHAR(100) UNIQUE NOT NULL,
    display_order       INT NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    image_url           VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_categories_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$' AND CHAR_LENGTH(slug) > 0),
    CONSTRAINT chk_categories_display_order CHECK (display_order >= 0),
    FOREIGN KEY (parent_id) REFERENCES product_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 상품 (단순화)
CREATE TABLE products (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                     VARCHAR(100) UNIQUE NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    short_description       VARCHAR(500),
    description             TEXT,
    category_id             BIGINT NULL,
    brand                   VARCHAR(100),
    product_type            ENUM('PHYSICAL', 'DIGITAL', 'SERVICE', 'BUNDLE') NOT NULL DEFAULT 'PHYSICAL',
    status                  ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK') NOT NULL DEFAULT 'DRAFT',
    
    -- 기본 가격 정보 (단순화)
    base_price              DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (base_price >= 0),
    sale_price              DECIMAL(12,4) CHECK (sale_price >= 0),
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 물리적 속성
    weight_grams            INT CHECK (weight_grams > 0),
    requires_shipping       BOOLEAN NOT NULL DEFAULT TRUE,
    is_taxable              BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- SEO 및 검색
    slug                    VARCHAR(300) UNIQUE NOT NULL,
    search_tags             TEXT,
    
    -- 미디어 (단순화)
    primary_image_url       VARCHAR(500),
    
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 1,
    
    CONSTRAINT chk_products_sale_price CHECK (sale_price IS NULL OR sale_price <= base_price),
    CONSTRAINT chk_products_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$' AND CHAR_LENGTH(slug) > 0),
    FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE SET NULL,
    
    FULLTEXT(name, short_description, description, search_tags)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 상품 변형 (단순화)
CREATE TABLE product_variants (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id              BIGINT NOT NULL,
    variant_sku             VARCHAR(100) UNIQUE NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    price_adjustment        DECIMAL(10,4) DEFAULT 0.00,
    status                  ENUM('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    is_default              BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 옵션 정보 (JSON으로 단순화)
    options                 JSON, -- {"color": "red", "size": "XL"}
    
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- ============================================================================
-- 3. 플랫폼 도메인 - 재고 관리 (Inventory Service)
-- ============================================================================

-- 재고 스냅샷 (현재 상태)
CREATE TABLE inventory_snapshots (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id              BIGINT NOT NULL,
    variant_id              BIGINT NULL,
    location_code           VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    available_quantity      INT NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity       INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    total_quantity          INT AS (available_quantity + reserved_quantity) STORED,
    low_stock_threshold     INT NOT NULL DEFAULT 10 CHECK (low_stock_threshold >= 0),
    stock_status            VARCHAR(20) AS (
        CASE 
            WHEN available_quantity = 0 THEN 'OUT_OF_STOCK'
            WHEN available_quantity <= low_stock_threshold THEN 'LOW_STOCK'
            ELSE 'IN_STOCK'
        END
    ) STORED,
    last_updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 1,
    
    UNIQUE KEY uk_inventory_location (product_id, variant_id, location_code),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 재고 이벤트 (Event Sourcing) - MySQL 9에서는 파티션을 따로 처리
CREATE TABLE inventory_events (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type              VARCHAR(50) NOT NULL,
    aggregate_id            VARCHAR(255) NOT NULL,
    aggregate_version       BIGINT NOT NULL CHECK (aggregate_version > 0),
    product_id              BIGINT NOT NULL,
    variant_id              BIGINT NULL,
    location_code           VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    quantity_change         INT NOT NULL,
    quantity_before         INT NOT NULL,
    quantity_after          INT NOT NULL,
    change_reason           VARCHAR(100),
    reference_type          VARCHAR(50),
    reference_id            VARCHAR(100),
    event_data              JSON,
    correlation_id          CHAR(36),
    occurred_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_inventory_events_quantities CHECK (
        quantity_after = quantity_before + quantity_change
    ),
    INDEX idx_inventory_events_occurred_at (occurred_at),
    INDEX idx_inventory_events_product (product_id, occurred_at),
    INDEX idx_inventory_events_aggregate (aggregate_id, aggregate_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- 4. 플랫폼 도메인 - 장바구니 관리 (Cart Service)
-- ============================================================================

-- 장바구니
CREATE TABLE shopping_carts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NULL,
    session_id    VARCHAR(255),
    status        ENUM('ACTIVE', 'ABANDONED', 'CONVERTED') NOT NULL DEFAULT 'ACTIVE',
    total_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    currency      VARCHAR(3) NOT NULL DEFAULT 'KRW',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 장바구니 아이템
CREATE TABLE shopping_cart_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id             BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    product_variant_id  BIGINT NULL,
    quantity            INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price          DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
    total_price         DECIMAL(10, 2) NOT NULL CHECK (total_price > 0),
    added_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. 주문 도메인 (Order Service)
-- ============================================================================

-- 주문 상태 유형 - MySQL에서는 ENUM 사용

-- 주문
CREATE TABLE orders (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_uuid              CHAR(36) UNIQUE NOT NULL DEFAULT (UUID()),
    order_number            VARCHAR(100) UNIQUE NOT NULL,
    user_id                 BIGINT NOT NULL,  -- Cross-domain reference
    status                  ENUM('PENDING', 'CONFIRMED', 'PAYMENT_PENDING', 'PAID', 'PROCESSING', 
                                'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    
    -- 주문 금액 정보
    subtotal_amount         DECIMAL(12,4) NOT NULL CHECK (subtotal_amount >= 0),
    tax_amount              DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    shipping_amount         DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (shipping_amount >= 0),
    discount_amount         DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount            DECIMAL(12,4) NOT NULL CHECK (total_amount >= 0),
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 배송 정보
    shipping_address        JSON NOT NULL,
    
    -- 날짜 정보
    order_date              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at            TIMESTAMP NULL,
    payment_completed_at    TIMESTAMP NULL,
    shipped_at              TIMESTAMP NULL,
    delivered_at            TIMESTAMP NULL,
    cancelled_at            TIMESTAMP NULL,
    
    -- 시스템 정보
    source_channel          VARCHAR(50) DEFAULT 'WEB',
    version                 BIGINT NOT NULL DEFAULT 1,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 주문 항목
CREATE TABLE order_items (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id                BIGINT NOT NULL,
    product_id              BIGINT NOT NULL,  -- Cross-domain reference
    product_variant_id      BIGINT NULL,      -- Cross-domain reference
    
    -- 스냅샷 정보
    product_name            VARCHAR(255) NOT NULL,
    product_sku             VARCHAR(100) NOT NULL,
    variant_name            VARCHAR(255),
    
    -- 수량 및 가격
    quantity                INT NOT NULL CHECK (quantity > 0),
    unit_price              DECIMAL(10,4) NOT NULL CHECK (unit_price >= 0),
    total_price             DECIMAL(12,4) NOT NULL CHECK (total_price >= 0),
    
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6. 결제 도메인 (Payment Service)
-- ============================================================================

-- 결제 상태 유형 - MySQL에서는 ENUM 사용
-- 결제 수단 유형 - MySQL에서는 ENUM 사용

-- 결제
CREATE TABLE payments (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_uuid           CHAR(36) UNIQUE NOT NULL DEFAULT (UUID()),
    order_id               VARCHAR(100) NOT NULL,  -- Cross-domain reference
    user_id                BIGINT NOT NULL,        -- Cross-domain reference
    
    -- 결제 금액 정보
    amount                 DECIMAL(12,4) NOT NULL CHECK (amount > 0),
    currency               CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 결제 상태 및 방법
    status                 ENUM('PENDING', 'AUTHORIZED', 'CAPTURED', 'PARTIAL_CAPTURED', 
                               'CANCELLED', 'FAILED', 'REFUNDED', 'PARTIAL_REFUNDED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    payment_method         ENUM('CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT',
                               'DIGITAL_WALLET', 'CRYPTOCURRENCY', 'POINT', 'GIFT_CARD') NOT NULL,
    payment_provider       VARCHAR(50) NOT NULL,
    
    -- PG사 정보
    external_payment_id    VARCHAR(100),
    gateway_transaction_id VARCHAR(100),
    approval_number        VARCHAR(50),
    
    -- 실패 정보
    failure_code           VARCHAR(20),
    failure_reason         TEXT,
    
    -- 환불 관련
    parent_payment_id      BIGINT NULL,
    refund_amount          DECIMAL(12,4) CHECK (refund_amount >= 0),
    refund_reason          TEXT,
    
    -- 결제 세부 정보
    payment_details        JSON,
    
    -- 시간 정보
    authorized_at          TIMESTAMP NULL,
    captured_at            TIMESTAMP NULL,
    cancelled_at           TIMESTAMP NULL,
    failed_at              TIMESTAMP NULL,
    refunded_at            TIMESTAMP NULL,
    
    version                BIGINT NOT NULL DEFAULT 1,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_payment_id) REFERENCES payments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- 7. 플랫폼 도메인 - 알림 관리 (Notification Service)
-- ============================================================================

-- 알림
CREATE TABLE notifications (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    type          VARCHAR(50) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    message       TEXT NOT NULL,
    data          JSON,
    is_read       BOOLEAN NOT NULL DEFAULT FALSE,
    read_at       TIMESTAMP NULL,
    is_sent       BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at       TIMESTAMP NULL,
    channel       VARCHAR(20) NOT NULL DEFAULT 'WEB',
    priority      ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL',
    expires_at    TIMESTAMP NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 8. 이벤트 저장소 및 Kafka 통합
-- ============================================================================

-- 도메인 이벤트 저장소
CREATE TABLE event_store (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id      VARCHAR(255) NOT NULL,
    aggregate_type    VARCHAR(100) NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    event_version     INT NOT NULL DEFAULT 1,
    event_data        JSON NOT NULL,
    metadata          JSON,
    occurred_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sequence_number   BIGINT NOT NULL,
    
    UNIQUE KEY uk_event_store_aggregate (aggregate_id, sequence_number),
    INDEX idx_event_store_occurred_at (occurred_at),
    INDEX idx_event_store_type (event_type, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 크로스 도메인 이벤트 매핑
CREATE TABLE cross_domain_events (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_uuid              CHAR(36) UNIQUE NOT NULL DEFAULT (UUID()),
    event_type              VARCHAR(100) NOT NULL,
    source_domain           VARCHAR(50) NOT NULL,
    target_domains          JSON NOT NULL,  -- MySQL에서는 JSON으로 배열 처리
    entity_type             VARCHAR(50) NOT NULL,
    entity_id               BIGINT NOT NULL,
    entity_uuid             CHAR(36),
    event_data              JSON NOT NULL,
    correlation_id          CHAR(36),
    kafka_topic             VARCHAR(100) NOT NULL,
    kafka_partition         INT,
    kafka_offset            BIGINT,
    publishing_status       ENUM('PENDING', 'PUBLISHED', 'FAILED', 'SKIPPED') NOT NULL DEFAULT 'PENDING',
    retry_count             INT NOT NULL DEFAULT 0,
    max_retries             INT NOT NULL DEFAULT 3,
    error_message           TEXT,
    occurred_at             TIMESTAMP NOT NULL,
    published_at            TIMESTAMP NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_cross_domain_events_occurred_at (occurred_at),
    INDEX idx_cross_domain_events_status (publishing_status, retry_count),
    INDEX idx_cross_domain_events_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 9. 머티리얼라이즈드 뷰 (읽기 최적화)
-- ============================================================================

-- 사용자 통합 뷰
CREATE TABLE mv_users (
    id                  BIGINT PRIMARY KEY,
    user_uuid           CHAR(36) UNIQUE NOT NULL,
    username            VARCHAR(50) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    full_name           VARCHAR(201),
    status              VARCHAR(20) NOT NULL,
    total_orders        BIGINT NOT NULL DEFAULT 0,
    total_spent         DECIMAL(15,4) NOT NULL DEFAULT 0,
    last_order_date     TIMESTAMP NULL,
    synced_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 통합 뷰
CREATE TABLE mv_products (
    id                      BIGINT PRIMARY KEY,
    sku                     VARCHAR(100) UNIQUE NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    status                  VARCHAR(20) NOT NULL,
    brand                   VARCHAR(100),
    category_name           VARCHAR(100),
    base_price              DECIMAL(12,4),
    sale_price              DECIMAL(12,4),
    currency                CHAR(3) DEFAULT 'KRW',
    available_stock         INT NOT NULL DEFAULT 0,
    stock_status            VARCHAR(20),
    primary_image_url       VARCHAR(500),
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    purchase_count          BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NULL,
    synced_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FULLTEXT(name, brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 주문 통합 뷰
CREATE TABLE mv_orders (
    id                      BIGINT PRIMARY KEY,
    order_uuid              CHAR(36) UNIQUE NOT NULL,
    order_number            VARCHAR(100) UNIQUE NOT NULL,
    user_id                 BIGINT NOT NULL,
    status                  VARCHAR(20) NOT NULL,
    total_amount            DECIMAL(12,4) NOT NULL,
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    payment_status          VARCHAR(20),
    order_date              TIMESTAMP NULL,
    synced_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 결제 통합 뷰
CREATE TABLE mv_payments (
    id                     BIGINT PRIMARY KEY,
    payment_uuid           CHAR(36) UNIQUE NOT NULL,
    order_id               VARCHAR(100) NOT NULL,
    user_id                BIGINT NOT NULL,
    amount                 DECIMAL(12,4) NOT NULL,
    status                 VARCHAR(20) NOT NULL,
    payment_method         VARCHAR(20) NOT NULL,
    payment_provider       VARCHAR(20) NOT NULL,
    captured_at            TIMESTAMP NULL,
    synced_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 일별 매출 통계
CREATE TABLE mv_daily_sales (
    date                    DATE PRIMARY KEY,
    total_orders            BIGINT NOT NULL DEFAULT 0,
    completed_orders        BIGINT NOT NULL DEFAULT 0,
    total_revenue           DECIMAL(15,4) NOT NULL DEFAULT 0,
    unique_customers        BIGINT NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 카탈로그 뷰
CREATE VIEW v_product_catalog AS
SELECT 
    p.id,
    p.sku,
    p.name,
    p.status,
    p.brand,
    pc.name as category_name,
    p.base_price,
    p.sale_price,
    p.currency,
    inv.available_quantity,
    inv.stock_status,
    p.primary_image_url,
    p.is_featured,
    CASE 
        WHEN p.sale_price IS NOT NULL AND p.sale_price < p.base_price 
        THEN ROUND(((p.base_price - p.sale_price) / p.base_price) * 100, 0)
        ELSE 0
    END as discount_percentage,
    p.created_at
FROM products p
LEFT JOIN product_categories pc ON p.category_id = pc.id
LEFT JOIN inventory_snapshots inv ON p.id = inv.product_id AND inv.variant_id IS NULL
WHERE p.status IN ('ACTIVE', 'OUT_OF_STOCK');

-- 재고 알림 뷰
CREATE VIEW v_inventory_alerts AS
SELECT 
    inv.id,
    inv.product_id,
    p.sku,
    p.name as product_name,
    inv.available_quantity,
    inv.low_stock_threshold,
    inv.stock_status,
    CASE 
        WHEN inv.available_quantity = 0 THEN 'CRITICAL'
        WHEN inv.available_quantity <= inv.low_stock_threshold THEN 'HIGH'
        ELSE 'MEDIUM'
    END as alert_level
FROM inventory_snapshots inv
JOIN products p ON inv.product_id = p.id
WHERE inv.stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK');


-- ============================================================================
-- 10. 인덱스 생성
-- ============================================================================

-- Users 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_uuid ON users(user_uuid);

-- User addresses 인덱스
CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);
CREATE INDEX idx_user_addresses_type ON user_addresses(address_type);

-- Categories 인덱스
CREATE INDEX idx_categories_parent_id ON product_categories(parent_id);
CREATE INDEX idx_categories_slug ON product_categories(slug);
CREATE INDEX idx_categories_active ON product_categories(is_active, is_featured);

-- Products 인덱스
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_category ON products(category_id, status);
CREATE INDEX idx_products_brand ON products(brand, status);
CREATE INDEX idx_products_featured ON products(is_featured, status);
-- 전체 텍스트 검색은 이미 테이블 생성 시 FULLTEXT 인덱스로 처리됨

-- Variants 인덱스
CREATE INDEX idx_variants_product ON product_variants(product_id, status);
CREATE INDEX idx_variants_sku ON product_variants(variant_sku);

-- Inventory 인덱스
CREATE INDEX idx_inventory_product ON inventory_snapshots(product_id);
CREATE INDEX idx_inventory_stock_status ON inventory_snapshots(stock_status);

-- Orders 인덱스
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_uuid ON orders(order_uuid);
CREATE INDEX idx_orders_date ON orders(order_date);

-- Order items 인덱스
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Payments 인덱스
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_uuid ON payments(payment_uuid);

-- Events 인덱스
CREATE INDEX idx_inventory_events_aggregate ON inventory_events(aggregate_id, aggregate_version);
CREATE INDEX idx_inventory_events_product ON inventory_events(product_id, occurred_at);
CREATE INDEX idx_event_store_aggregate_id ON event_store(aggregate_id);
CREATE INDEX idx_cross_domain_events_type ON cross_domain_events(event_type, source_domain);
CREATE INDEX idx_cross_domain_events_status ON cross_domain_events(publishing_status);

-- Cart 인덱스
CREATE INDEX idx_shopping_carts_user_id ON shopping_carts(user_id);
CREATE INDEX idx_cart_items_cart_id ON shopping_cart_items(cart_id);

-- Notifications 인덱스
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);

-- Materialized Views 인덱스
CREATE INDEX idx_mv_users_uuid ON mv_users(user_uuid);
CREATE INDEX idx_mv_products_sku ON mv_products(sku);
CREATE INDEX idx_mv_products_status ON mv_products(status);
CREATE INDEX idx_mv_orders_user_id ON mv_orders(user_id);
CREATE INDEX idx_mv_payments_order_id ON mv_payments(order_id);

-- ============================================================================
-- 11. 트리거 및 함수
-- ============================================================================

-- MySQL에서는 ON UPDATE CURRENT_TIMESTAMP로 처리됨 (이미 테이블 생성 시 설정됨)

-- MySQL에서는 FULLTEXT 인덱스로 전체 텍스트 검색 처리

-- 크로스 도메인 이벤트 발행 프로시저 (MySQL 버전)
DELIMITER //

CREATE PROCEDURE publish_cross_domain_event(
    IN p_event_type VARCHAR(100),
    IN p_source_domain VARCHAR(50),
    IN p_entity_type VARCHAR(50),
    IN p_entity_id BIGINT,
    IN p_entity_uuid CHAR(36),
    IN p_event_data JSON,
    IN p_correlation_id CHAR(36)
)
BEGIN
    DECLARE v_event_uuid CHAR(36);
    
    SET v_event_uuid = UUID();
    
    INSERT INTO cross_domain_events (
        event_uuid, event_type, source_domain, target_domains,
        entity_type, entity_id, entity_uuid, event_data,
        correlation_id, kafka_topic, occurred_at, created_at
    ) VALUES (
        v_event_uuid, p_event_type, p_source_domain, JSON_ARRAY('ORDER', 'PAYMENT', 'MATERIALIZED'),
        p_entity_type, p_entity_id, p_entity_uuid, p_event_data,
        p_correlation_id, CONCAT(LOWER(p_source_domain), '.', LOWER(p_event_type)), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );
    
    SELECT v_event_uuid as event_uuid;
END//

DELIMITER ;

-- ============================================================================
-- 12. 초기 데이터 삽입
-- ============================================================================

-- 기본 카테고리 생성
INSERT INTO product_categories (name, description, slug, display_order, is_active) VALUES
('Electronics', '전자제품 및 IT 기기', 'electronics', 1, TRUE),
('Fashion', '패션 및 의류', 'fashion', 2, TRUE),
('Home & Garden', '홈 인테리어 및 가든 용품', 'home-garden', 3, TRUE),
('Books & Media', '도서 및 미디어', 'books-media', 4, TRUE),
('Sports & Outdoor', '스포츠 및 아웃도어 용품', 'sports-outdoor', 5, TRUE);

-- 오늘 날짜의 일별 통계 초기화
INSERT IGNORE INTO mv_daily_sales (date) 
VALUES (CURRENT_DATE);

-- ============================================================================
-- 13. 스키마 정보
-- ============================================================================

SELECT '✅ MSA Commerce Lab - 통합 MySQL 9.0 데이터베이스 스키마 생성 완료' as status,
       'Features: Event Sourcing, Materialized Views, Cross-Domain Events, Kafka Integration' as features;