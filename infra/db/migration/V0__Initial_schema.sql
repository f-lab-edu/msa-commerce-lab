-- ============================================================================
-- MSA Commerce Lab - 통합 PostgreSQL 데이터베이스 스키마
-- Version: V0 (Unified Schema for All Microservices)
-- Date: 2025-01-20
-- Description: 마이크로서비스 아키텍처를 위한 통합 PostgreSQL 스키마
-- Services: Platform, Order, Payment, Materialized View
-- ============================================================================

-- PostgreSQL 확장 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- ============================================================================
-- 1. 플랫폼 도메인 - 사용자 관리 (Auth Service, User Service)
-- ============================================================================

-- 사용자 계정
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    user_uuid           UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    username            VARCHAR(50) UNIQUE NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20),
    date_of_birth       DATE,
    gender              VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
                       CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
    email_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    profile_image_url   VARCHAR(500),
    last_login_at       TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 주소
CREATE TABLE user_addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_type    VARCHAR(20) NOT NULL DEFAULT 'HOME' 
                   CHECK (address_type IN ('HOME', 'WORK', 'BILLING', 'SHIPPING', 'OTHER')),
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
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 2. 플랫폼 도메인 - 상품 관리 (Product Service) - 단순화
-- ============================================================================

-- 상품 카테고리 (단순화)
CREATE TABLE product_categories (
    id                  BIGSERIAL PRIMARY KEY,
    parent_id           BIGINT REFERENCES product_categories(id) ON DELETE SET NULL,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    slug                VARCHAR(100) UNIQUE NOT NULL,
    display_order       INTEGER NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    image_url           VARCHAR(500),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_categories_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$' AND LENGTH(slug) > 0),
    CONSTRAINT chk_categories_display_order CHECK (display_order >= 0)
);


-- 상품 (단순화)
CREATE TABLE products (
    id                      BIGSERIAL PRIMARY KEY,
    sku                     VARCHAR(100) UNIQUE NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    short_description       VARCHAR(500),
    description             TEXT,
    category_id             BIGINT REFERENCES product_categories(id) ON DELETE SET NULL,
    brand                   VARCHAR(100),
    product_type            VARCHAR(20) NOT NULL DEFAULT 'PHYSICAL' 
                           CHECK (product_type IN ('PHYSICAL', 'DIGITAL', 'SERVICE', 'BUNDLE')),
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT' 
                           CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK')),
    
    -- 기본 가격 정보 (단순화)
    base_price              DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (base_price >= 0),
    sale_price              DECIMAL(12,4) CHECK (sale_price >= 0),
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 물리적 속성
    weight_grams            INTEGER CHECK (weight_grams > 0),
    requires_shipping       BOOLEAN NOT NULL DEFAULT TRUE,
    is_taxable              BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- SEO 및 검색
    slug                    VARCHAR(300) UNIQUE NOT NULL,
    search_tags             TEXT,
    search_vector           TSVECTOR,
    
    -- 미디어 (단순화)
    primary_image_url       VARCHAR(500),
    
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 1,
    
    CONSTRAINT chk_products_sale_price CHECK (sale_price IS NULL OR sale_price <= base_price),
    CONSTRAINT chk_products_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$' AND LENGTH(slug) > 0)
);


-- 상품 변형 (단순화)
CREATE TABLE product_variants (
    id                      BIGSERIAL PRIMARY KEY,
    product_id              BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_sku             VARCHAR(100) UNIQUE NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    price_adjustment        DECIMAL(10,4) DEFAULT 0.00,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
                           CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK')),
    is_default              BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 옵션 정보 (JSON으로 단순화)
    options                 JSONB, -- {"color": "red", "size": "XL"}
    
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);



-- ============================================================================
-- 3. 플랫폼 도메인 - 재고 관리 (Inventory Service)
-- ============================================================================

-- 재고 스냅샷 (현재 상태)
CREATE TABLE inventory_snapshots (
    id                      BIGSERIAL PRIMARY KEY,
    product_id              BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id              BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    location_code           VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    available_quantity      INTEGER NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity       INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    total_quantity          INTEGER GENERATED ALWAYS AS (available_quantity + reserved_quantity) STORED,
    low_stock_threshold     INTEGER NOT NULL DEFAULT 10 CHECK (low_stock_threshold >= 0),
    stock_status            VARCHAR(20) GENERATED ALWAYS AS (
        CASE 
            WHEN available_quantity = 0 THEN 'OUT_OF_STOCK'
            WHEN available_quantity <= low_stock_threshold THEN 'LOW_STOCK'
            ELSE 'IN_STOCK'
        END
    ) STORED,
    last_updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 1,
    
    UNIQUE (product_id, variant_id, location_code)
);

-- 재고 이벤트 (Event Sourcing)
CREATE TABLE inventory_events (
    id                      BIGSERIAL PRIMARY KEY,
    event_type              VARCHAR(50) NOT NULL,
    aggregate_id            VARCHAR(255) NOT NULL,
    aggregate_version       BIGINT NOT NULL CHECK (aggregate_version > 0),
    product_id              BIGINT NOT NULL,
    variant_id              BIGINT,
    location_code           VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    quantity_change         INTEGER NOT NULL,
    quantity_before         INTEGER NOT NULL,
    quantity_after          INTEGER NOT NULL,
    change_reason           VARCHAR(100),
    reference_type          VARCHAR(50),
    reference_id            VARCHAR(100),
    event_data              JSONB,
    correlation_id          UUID,
    occurred_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_inventory_events_quantities CHECK (
        quantity_after = quantity_before + quantity_change
    )
) PARTITION BY RANGE (occurred_at);

-- 재고 이벤트 파티션 (월별)
CREATE TABLE inventory_events_y2025m01 PARTITION OF inventory_events
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE inventory_events_y2025m02 PARTITION OF inventory_events
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');


-- ============================================================================
-- 4. 플랫폼 도메인 - 장바구니 관리 (Cart Service)
-- ============================================================================

-- 장바구니
CREATE TABLE shopping_carts (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_id    VARCHAR(255),
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
                 CHECK (status IN ('ACTIVE', 'ABANDONED', 'CONVERTED')),
    total_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    currency      VARCHAR(3) NOT NULL DEFAULT 'KRW',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP WITH TIME ZONE
);

-- 장바구니 아이템
CREATE TABLE shopping_cart_items (
    id                  BIGSERIAL PRIMARY KEY,
    cart_id             BIGINT NOT NULL REFERENCES shopping_carts(id) ON DELETE CASCADE,
    product_id          BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    product_variant_id  BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity            INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price          DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
    total_price         DECIMAL(10, 2) NOT NULL CHECK (total_price > 0),
    added_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 5. 주문 도메인 (Order Service)
-- ============================================================================

-- 주문 상태 유형
CREATE TYPE order_status_type AS ENUM (
    'PENDING', 'CONFIRMED', 'PAYMENT_PENDING', 'PAID', 'PROCESSING',
    'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED', 'FAILED'
);

-- 주문
CREATE TABLE orders (
    id                      BIGSERIAL PRIMARY KEY,
    order_uuid              UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    order_number            VARCHAR(100) UNIQUE NOT NULL,
    user_id                 BIGINT NOT NULL,  -- Cross-domain reference
    status                  order_status_type NOT NULL DEFAULT 'PENDING',
    
    -- 주문 금액 정보
    subtotal_amount         DECIMAL(12,4) NOT NULL CHECK (subtotal_amount >= 0),
    tax_amount              DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    shipping_amount         DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (shipping_amount >= 0),
    discount_amount         DECIMAL(12,4) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount            DECIMAL(12,4) NOT NULL CHECK (total_amount >= 0),
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 배송 정보
    shipping_address        JSONB NOT NULL,
    
    -- 날짜 정보
    order_date              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at            TIMESTAMP WITH TIME ZONE,
    payment_completed_at    TIMESTAMP WITH TIME ZONE,
    shipped_at              TIMESTAMP WITH TIME ZONE,
    delivered_at            TIMESTAMP WITH TIME ZONE,
    cancelled_at            TIMESTAMP WITH TIME ZONE,
    
    -- 시스템 정보
    source_channel          VARCHAR(50) DEFAULT 'WEB',
    version                 BIGINT NOT NULL DEFAULT 1,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 주문 항목
CREATE TABLE order_items (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id              BIGINT NOT NULL,  -- Cross-domain reference
    product_variant_id      BIGINT,           -- Cross-domain reference
    
    -- 스냅샷 정보
    product_name            VARCHAR(255) NOT NULL,
    product_sku             VARCHAR(100) NOT NULL,
    variant_name            VARCHAR(255),
    
    -- 수량 및 가격
    quantity                INTEGER NOT NULL CHECK (quantity > 0),
    unit_price              DECIMAL(10,4) NOT NULL CHECK (unit_price >= 0),
    total_price             DECIMAL(12,4) NOT NULL CHECK (total_price >= 0),
    
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 6. 결제 도메인 (Payment Service)
-- ============================================================================

-- 결제 상태 유형
CREATE TYPE payment_status_type AS ENUM (
    'PENDING', 'AUTHORIZED', 'CAPTURED', 'PARTIAL_CAPTURED',
    'CANCELLED', 'FAILED', 'REFUNDED', 'PARTIAL_REFUNDED', 'EXPIRED'
);

-- 결제 수단 유형
CREATE TYPE payment_method_type AS ENUM (
    'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT',
    'DIGITAL_WALLET', 'CRYPTOCURRENCY', 'POINT', 'GIFT_CARD'
);

-- 결제
CREATE TABLE payments (
    id                     BIGSERIAL PRIMARY KEY,
    payment_uuid           UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    order_id               VARCHAR(100) NOT NULL,  -- Cross-domain reference
    user_id                BIGINT NOT NULL,        -- Cross-domain reference
    
    -- 결제 금액 정보
    amount                 DECIMAL(12,4) NOT NULL CHECK (amount > 0),
    currency               CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 결제 상태 및 방법
    status                 payment_status_type NOT NULL DEFAULT 'PENDING',
    payment_method         payment_method_type NOT NULL,
    payment_provider       VARCHAR(50) NOT NULL,
    
    -- PG사 정보
    external_payment_id    VARCHAR(100),
    gateway_transaction_id VARCHAR(100),
    approval_number        VARCHAR(50),
    
    -- 실패 정보
    failure_code           VARCHAR(20),
    failure_reason         TEXT,
    
    -- 환불 관련
    parent_payment_id      BIGINT REFERENCES payments(id),
    refund_amount          DECIMAL(12,4) CHECK (refund_amount >= 0),
    refund_reason          TEXT,
    
    -- 결제 세부 정보
    payment_details        JSONB,
    
    -- 시간 정보
    authorized_at          TIMESTAMP WITH TIME ZONE,
    captured_at            TIMESTAMP WITH TIME ZONE,
    cancelled_at           TIMESTAMP WITH TIME ZONE,
    failed_at              TIMESTAMP WITH TIME ZONE,
    refunded_at            TIMESTAMP WITH TIME ZONE,
    
    version                BIGINT NOT NULL DEFAULT 1,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================================
-- 7. 플랫폼 도메인 - 알림 관리 (Notification Service)
-- ============================================================================

-- 알림
CREATE TABLE notifications (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(50) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    message       TEXT NOT NULL,
    data          JSONB,
    is_read       BOOLEAN NOT NULL DEFAULT FALSE,
    read_at       TIMESTAMP WITH TIME ZONE,
    is_sent       BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at       TIMESTAMP WITH TIME ZONE,
    channel       VARCHAR(20) NOT NULL DEFAULT 'WEB',
    priority      VARCHAR(10) NOT NULL DEFAULT 'NORMAL' 
                 CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    expires_at    TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 8. 이벤트 저장소 및 Kafka 통합
-- ============================================================================

-- 도메인 이벤트 저장소
CREATE TABLE event_store (
    id                BIGSERIAL PRIMARY KEY,
    aggregate_id      VARCHAR(255) NOT NULL,
    aggregate_type    VARCHAR(100) NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    event_version     INTEGER NOT NULL DEFAULT 1,
    event_data        JSONB NOT NULL,
    metadata          JSONB,
    occurred_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sequence_number   BIGINT NOT NULL,
    
    UNIQUE (aggregate_id, sequence_number)
) PARTITION BY RANGE (occurred_at);

-- 이벤트 저장소 파티션
CREATE TABLE event_store_y2025m01 PARTITION OF event_store
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE event_store_y2025m02 PARTITION OF event_store
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- 크로스 도메인 이벤트 매핑
CREATE TABLE cross_domain_events (
    id                      BIGSERIAL PRIMARY KEY,
    event_uuid              UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    event_type              VARCHAR(100) NOT NULL,
    source_domain           VARCHAR(50) NOT NULL,
    target_domains          VARCHAR(50)[] NOT NULL,
    entity_type             VARCHAR(50) NOT NULL,
    entity_id               BIGINT NOT NULL,
    entity_uuid             UUID,
    event_data              JSONB NOT NULL,
    correlation_id          UUID,
    kafka_topic             VARCHAR(100) NOT NULL,
    kafka_partition         INTEGER,
    kafka_offset            BIGINT,
    publishing_status       VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
                           CHECK (publishing_status IN ('PENDING', 'PUBLISHED', 'FAILED', 'SKIPPED')),
    retry_count             INTEGER NOT NULL DEFAULT 0,
    max_retries             INTEGER NOT NULL DEFAULT 3,
    error_message           TEXT,
    occurred_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at            TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (occurred_at);

-- 크로스 도메인 이벤트 파티션
CREATE TABLE cross_domain_events_y2025m01 PARTITION OF cross_domain_events
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE cross_domain_events_y2025m02 PARTITION OF cross_domain_events
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- ============================================================================
-- 9. 머티리얼라이즈드 뷰 (읽기 최적화)
-- ============================================================================

-- 사용자 통합 뷰
CREATE TABLE mv_users (
    id                  BIGINT PRIMARY KEY,
    user_uuid           UUID UNIQUE NOT NULL,
    username            VARCHAR(50) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    full_name           VARCHAR(201),
    status              VARCHAR(20) NOT NULL,
    total_orders        BIGINT NOT NULL DEFAULT 0,
    total_spent         DECIMAL(15,4) NOT NULL DEFAULT 0,
    last_order_date     TIMESTAMP WITH TIME ZONE,
    synced_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
    available_stock         INTEGER NOT NULL DEFAULT 0,
    stock_status            VARCHAR(20),
    primary_image_url       VARCHAR(500),
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    search_vector           TSVECTOR,
    purchase_count          BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP WITH TIME ZONE,
    synced_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 주문 통합 뷰
CREATE TABLE mv_orders (
    id                      BIGINT PRIMARY KEY,
    order_uuid              UUID UNIQUE NOT NULL,
    order_number            VARCHAR(100) UNIQUE NOT NULL,
    user_id                 BIGINT NOT NULL,
    status                  VARCHAR(20) NOT NULL,
    total_amount            DECIMAL(12,4) NOT NULL,
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    payment_status          VARCHAR(20),
    order_date              TIMESTAMP WITH TIME ZONE,
    synced_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 결제 통합 뷰
CREATE TABLE mv_payments (
    id                     BIGINT PRIMARY KEY,
    payment_uuid           UUID UNIQUE NOT NULL,
    order_id               VARCHAR(100) NOT NULL,
    user_id                BIGINT NOT NULL,
    amount                 DECIMAL(12,4) NOT NULL,
    status                 VARCHAR(20) NOT NULL,
    payment_method         VARCHAR(20) NOT NULL,
    payment_provider       VARCHAR(20) NOT NULL,
    captured_at            TIMESTAMP WITH TIME ZONE,
    synced_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 일별 매출 통계
CREATE TABLE mv_daily_sales (
    date                    DATE PRIMARY KEY,
    total_orders            BIGINT NOT NULL DEFAULT 0,
    completed_orders        BIGINT NOT NULL DEFAULT 0,
    total_revenue           DECIMAL(15,4) NOT NULL DEFAULT 0,
    unique_customers        BIGINT NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
CREATE GIN INDEX idx_products_search ON products USING GIN(search_vector);

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

-- 업데이트 시간 자동 갱신 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 트리거 생성 (updated_at 자동 갱신)
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_addresses_updated_at BEFORE UPDATE ON user_addresses FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_product_categories_updated_at BEFORE UPDATE ON product_categories FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_product_variants_updated_at BEFORE UPDATE ON product_variants FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_shopping_carts_updated_at BEFORE UPDATE ON shopping_carts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_shopping_cart_items_updated_at BEFORE UPDATE ON shopping_cart_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 전체 텍스트 검색 벡터 업데이트 함수
CREATE OR REPLACE FUNCTION update_product_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := to_tsvector('korean', 
        COALESCE(NEW.name, '') || ' ' || 
        COALESCE(NEW.short_description, '') || ' ' || 
        COALESCE(NEW.brand, '') || ' ' || 
        COALESCE(NEW.search_tags, '')
    );
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 상품 검색 벡터 트리거
CREATE TRIGGER update_products_search_vector 
    BEFORE INSERT OR UPDATE ON products 
    FOR EACH ROW EXECUTE FUNCTION update_product_search_vector();

-- 크로스 도메인 이벤트 발행 함수
CREATE OR REPLACE FUNCTION publish_cross_domain_event(
    p_event_type VARCHAR(100),
    p_source_domain VARCHAR(50),
    p_entity_type VARCHAR(50),
    p_entity_id BIGINT,
    p_entity_uuid UUID,
    p_event_data JSONB,
    p_correlation_id UUID DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    v_event_uuid UUID;
BEGIN
    v_event_uuid := uuid_generate_v4();
    
    INSERT INTO cross_domain_events (
        event_uuid, event_type, source_domain, target_domains,
        entity_type, entity_id, entity_uuid, event_data,
        correlation_id, kafka_topic, occurred_at, created_at
    ) VALUES (
        v_event_uuid, p_event_type, p_source_domain, ARRAY['ORDER', 'PAYMENT', 'MATERIALIZED']::VARCHAR[],
        p_entity_type, p_entity_id, p_entity_uuid, p_event_data,
        p_correlation_id, LOWER(p_source_domain) || '.' || LOWER(p_event_type), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );
    
    RETURN v_event_uuid;
END;
$$ LANGUAGE plpgsql;

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
INSERT INTO mv_daily_sales (date) 
VALUES (CURRENT_DATE) 
ON CONFLICT (date) DO NOTHING;

-- ============================================================================
-- 13. 스키마 정보
-- ============================================================================

SELECT '✅ MSA Commerce Lab - 통합 PostgreSQL 데이터베이스 스키마 생성 완료' as status,
       'Features: Event Sourcing, Materialized Views, Cross-Domain Events, Kafka Integration' as features;