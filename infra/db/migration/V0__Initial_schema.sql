-- ============================================================================
-- MSA Commerce Lab - 분리된 MySQL 데이터베이스 스키마
-- Version: V0 (Database-Separated Schema for Microservices)
-- Date: 2025-01-21
-- Description: 마이크로서비스 아키텍처를 위한 분리된 MySQL 9.0 스키마
-- Databases: db_platform, db_order, db_payment
-- ============================================================================

-- MySQL 설정
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';
SET GLOBAL innodb_file_per_table = ON;
SET GLOBAL innodb_default_row_format = dynamic;

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS db_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_materialized_view CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================================
-- 1. PLATFORM DATABASE (db_platform)
-- ============================================================================

USE db_platform;

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
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_status (status),
    INDEX idx_users_uuid (user_uuid)
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
    
    INDEX idx_user_addresses_user_id (user_id),
    INDEX idx_user_addresses_type (address_type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 카테고리
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
    
    INDEX idx_categories_parent_id (parent_id),
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_active (is_active, is_featured),
    CONSTRAINT chk_categories_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$' AND CHAR_LENGTH(slug) > 0),
    CONSTRAINT chk_categories_display_order CHECK (display_order >= 0),
    FOREIGN KEY (parent_id) REFERENCES product_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품
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
    
    -- 기본 가격 정보
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
    
    -- 미디어
    primary_image_url       VARCHAR(500),
    
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version                 BIGINT NOT NULL DEFAULT 1,
    
    INDEX idx_products_sku (sku),
    INDEX idx_products_status (status),
    INDEX idx_products_category (category_id, status),
    INDEX idx_products_brand (brand, status),
    INDEX idx_products_featured (is_featured, status),
    FULLTEXT(name, short_description, description, search_tags),
    CONSTRAINT chk_products_sale_price CHECK (sale_price IS NULL OR sale_price <= base_price),
    CONSTRAINT chk_products_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$' AND CHAR_LENGTH(slug) > 0),
    FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 상품 변형
CREATE TABLE product_variants (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id              BIGINT NOT NULL,
    variant_sku             VARCHAR(100) UNIQUE NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    price_adjustment        DECIMAL(10,4) DEFAULT 0.00,
    status                  ENUM('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    is_default              BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 옵션 정보 (JSON)
    options                 JSON, -- {"color": "red", "size": "XL"}
    
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_variants_product (product_id, status),
    INDEX idx_variants_sku (variant_sku),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 재고 스냅샷
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
    
    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_stock_status (stock_status),
    UNIQUE KEY uk_inventory_location (product_id, variant_id, location_code),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 재고 이벤트 (Event Sourcing)
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
    
    INDEX idx_inventory_events_occurred_at (occurred_at),
    INDEX idx_inventory_events_product (product_id, occurred_at),
    INDEX idx_inventory_events_aggregate (aggregate_id, aggregate_version),
    CONSTRAINT chk_inventory_events_quantities CHECK (
        quantity_after = quantity_before + quantity_change
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    
    INDEX idx_shopping_carts_user_id (user_id),
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
    
    INDEX idx_cart_items_cart_id (cart_id),
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_type (type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 플랫폼 도메인 이벤트 저장소
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
    
    INDEX idx_event_store_aggregate_id (aggregate_id),
    INDEX idx_event_store_occurred_at (occurred_at),
    INDEX idx_event_store_type (event_type, occurred_at),
    UNIQUE KEY uk_event_store_aggregate (aggregate_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 크로스 도메인 이벤트 매핑
CREATE TABLE cross_domain_events (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_uuid              CHAR(36) UNIQUE NOT NULL DEFAULT (UUID()),
    event_type              VARCHAR(100) NOT NULL,
    source_domain           VARCHAR(50) NOT NULL,
    target_domains          JSON NOT NULL,
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
    INDEX idx_cross_domain_events_entity (entity_type, entity_id),
    INDEX idx_cross_domain_events_type (event_type, source_domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기본 카테고리 생성
INSERT INTO product_categories (name, description, slug, display_order, is_active) VALUES
('Electronics', '전자제품 및 IT 기기', 'electronics', 1, TRUE),
('Fashion', '패션 및 의류', 'fashion', 2, TRUE),
('Home & Garden', '홈 인테리어 및 가든 용품', 'home-garden', 3, TRUE),
('Books & Media', '도서 및 미디어', 'books-media', 4, TRUE),
('Sports & Outdoor', '스포츠 및 아웃도어 용품', 'sports-outdoor', 5, TRUE);

-- ============================================================================
-- 2. ORDER DATABASE (db_order)
-- ============================================================================

USE db_order;

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
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_uuid (order_uuid),
    INDEX idx_orders_date (order_date)
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
    
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 주문 도메인 이벤트 저장소
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
    
    INDEX idx_event_store_aggregate_id (aggregate_id),
    INDEX idx_event_store_occurred_at (occurred_at),
    INDEX idx_event_store_type (event_type, occurred_at),
    UNIQUE KEY uk_event_store_aggregate (aggregate_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. PAYMENT DATABASE (db_payment)
-- ============================================================================

USE db_payment;

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
    
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_user_id (user_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_uuid (payment_uuid),
    FOREIGN KEY (parent_payment_id) REFERENCES payments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 결제 도메인 이벤트 저장소
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
    
    INDEX idx_event_store_aggregate_id (aggregate_id),
    INDEX idx_event_store_occurred_at (occurred_at),
    INDEX idx_event_store_type (event_type, occurred_at),
    UNIQUE KEY uk_event_store_aggregate (aggregate_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. MATERIALIZED VIEW DATABASE (읽기 최적화) - Optional
-- ============================================================================

USE db_materialized_view;

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
    synced_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_mv_users_uuid (user_uuid)
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
    
    INDEX idx_mv_products_sku (sku),
    INDEX idx_mv_products_status (status),
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
    synced_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_mv_orders_user_id (user_id)
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
    synced_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_mv_payments_order_id (order_id)
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

-- 오늘 날짜의 일별 통계 초기화
INSERT IGNORE INTO mv_daily_sales (date) 
VALUES (CURRENT_DATE);

-- ============================================================================
-- 스키마 정보
-- ============================================================================

SELECT '✅ MSA Commerce Lab - 분리된 MySQL 9.0 데이터베이스 스키마 생성 완료' as status,
       'Databases: db_platform, db_order, db_payment, db_materialized_view' as database_list,
       'Features: Event Sourcing per Database, Cross-Domain Events, Embedded Indexes' as features;