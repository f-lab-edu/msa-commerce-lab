-- ============================================================================
-- MSA Commerce Lab - 자체물류 이커머스 초기 스키마 (단순화)
-- Version: V0 (Initial Schema - Simplified for Self-Logistics)
-- Date: 2025-01-20
-- Description: 자체물류용 단순화된 이커머스 스키마
-- ============================================================================

-- MySQL 9 호환성 설정
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET SESSION innodb_strict_mode = ON;

-- ============================================================================
-- 1. 사용자 관리 테이블
-- ============================================================================

-- Users table
CREATE TABLE users
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    phone_number     VARCHAR(20),
    date_of_birth    DATE,
    status           ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    email_verified   BOOLEAN      NOT NULL                                      DEFAULT FALSE,
    phone_verified   BOOLEAN      NOT NULL                                      DEFAULT FALSE,
    created_at       DATETIME     NOT NULL                                      DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at    DATETIME,
    profile_image_url VARCHAR(255),
    
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_status (status),
    INDEX idx_users_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- User addresses
CREATE TABLE user_addresses
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    address_type    ENUM ('HOME', 'WORK', 'BILLING', 'SHIPPING', 'OTHER') NOT NULL DEFAULT 'HOME',
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    recipient_name  VARCHAR(100) NOT NULL,
    phone_number    VARCHAR(20),
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    state_province  VARCHAR(100),
    postal_code     VARCHAR(20)  NOT NULL,
    country         VARCHAR(100) NOT NULL DEFAULT 'Korea',
    latitude        DECIMAL(10, 8),
    longitude       DECIMAL(11, 8),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_user_addresses_user_id (user_id),
    INDEX idx_user_addresses_type (address_type),
    INDEX idx_user_addresses_default (is_default),
    INDEX idx_user_addresses_location (latitude, longitude)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 2. 상품 관리 테이블
-- ============================================================================

-- Product categories
CREATE TABLE product_categories
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id       BIGINT,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    image_url       VARCHAR(255),
    sort_order      INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    seo_title       VARCHAR(255),
    seo_description TEXT,
    seo_keywords    VARCHAR(255),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES product_categories (id) ON DELETE SET NULL,
    INDEX idx_product_categories_parent_id (parent_id),
    INDEX idx_product_categories_slug (slug),
    INDEX idx_product_categories_active (is_active),
    INDEX idx_product_categories_sort_order (sort_order)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Products table
CREATE TABLE products
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id           BIGINT,
    name                  VARCHAR(255) NOT NULL,
    description           TEXT,
    short_description     VARCHAR(500),
    sku                   VARCHAR(100) NOT NULL UNIQUE,
    brand                 VARCHAR(100),
    model                 VARCHAR(100),
    
    -- 가격 정보
    base_price            DECIMAL(10, 2) NOT NULL,
    sale_price            DECIMAL(10, 2),
    cost_price            DECIMAL(10, 2),
    
    -- 물리적 정보
    weight                DECIMAL(8, 3),
    dimensions_length     DECIMAL(8, 2),
    dimensions_width      DECIMAL(8, 2),
    dimensions_height     DECIMAL(8, 2),
    
    -- 상태 및 설정
    status                ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'DISCONTINUED') NOT NULL DEFAULT 'DRAFT',
    is_digital            BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured           BOOLEAN NOT NULL DEFAULT FALSE,
    requires_shipping     BOOLEAN NOT NULL DEFAULT TRUE,
    is_taxable            BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- SEO
    slug                  VARCHAR(255) NOT NULL UNIQUE,
    meta_title            VARCHAR(255),
    meta_description      TEXT,
    meta_keywords         VARCHAR(255),
    
    -- 타임스탬프
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES product_categories (id) ON DELETE SET NULL,
    INDEX idx_products_category_id (category_id),
    INDEX idx_products_sku (sku),
    INDEX idx_products_status (status),
    INDEX idx_products_slug (slug),
    INDEX idx_products_featured (is_featured),
    INDEX idx_products_price (base_price, sale_price)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Product variants
CREATE TABLE product_variants
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id          BIGINT NOT NULL,
    name                VARCHAR(255) NOT NULL,
    sku                 VARCHAR(100) NOT NULL UNIQUE,
    
    -- 가격 (부모 제품 가격 오버라이드)
    price_adjustment    DECIMAL(10, 2) DEFAULT 0.00,
    weight_adjustment   DECIMAL(8, 3) DEFAULT 0.000,
    
    -- 옵션값들 (JSON으로 저장)
    option_values       JSON,
    
    -- 상태
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order          INT NOT NULL DEFAULT 0,
    
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    INDEX idx_product_variants_product_id (product_id),
    INDEX idx_product_variants_sku (sku),
    INDEX idx_product_variants_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Product images
CREATE TABLE product_images
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id   BIGINT       NOT NULL,
    variant_id   BIGINT       NULL,
    image_url    VARCHAR(255) NOT NULL,
    alt_text     VARCHAR(255),
    sort_order   INT          NOT NULL DEFAULT 0,
    is_primary   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    INDEX idx_product_images_product_id (product_id),
    INDEX idx_product_images_variant_id (variant_id),
    INDEX idx_product_images_sort_order (sort_order),
    INDEX idx_product_images_primary (is_primary)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 3. 단순화된 재고 관리 테이블 (자체물류용)
-- ============================================================================

-- 1. 단순화된 재고 관리 테이블
CREATE TABLE IF NOT EXISTS product_inventories
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id            BIGINT NOT NULL COMMENT '상품 ID',
    product_variant_id    BIGINT NULL COMMENT '상품 변형 ID (옵션)',
    
    -- 재고 수량
    available_quantity    INT NOT NULL DEFAULT 0 COMMENT '가용 재고',
    reserved_quantity     INT NOT NULL DEFAULT 0 COMMENT '예약된 재고', 
    total_quantity        INT NOT NULL DEFAULT 0 COMMENT '전체 재고',
    
    -- 비즈니스 규칙
    low_stock_threshold   INT NOT NULL DEFAULT 10 COMMENT '재고 부족 임계값',
    reorder_point         INT NOT NULL DEFAULT 5 COMMENT '재주문 포인트',
    min_order_quantity    INT NOT NULL DEFAULT 1 COMMENT '최소 주문 수량',
    max_order_quantity    INT NULL COMMENT '최대 주문 수량',
    
    -- 위치 정보 (단순화)
    location_code         VARCHAR(20) DEFAULT 'MAIN' COMMENT '보관 위치',
    
    -- 설정
    is_tracking_enabled   BOOLEAN NOT NULL DEFAULT TRUE COMMENT '재고 추적 여부',
    is_backorder_allowed  BOOLEAN NOT NULL DEFAULT FALSE COMMENT '품절 주문 허용',
    is_active             BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    
    -- 동시성 제어
    version_number        BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금 버전',
    
    -- 타임스탬프
    created_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- 제약조건
    UNIQUE KEY uk_product_inventories_product_variant (product_id, product_variant_id),
    
    -- 외래키
    CONSTRAINT fk_product_inventories_product 
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_inventories_variant 
        FOREIGN KEY (product_variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    
    -- 데이터 무결성
    CONSTRAINT chk_product_inventories_quantities 
        CHECK (total_quantity = (available_quantity + reserved_quantity)) ENFORCED,
    CONSTRAINT chk_product_inventories_positive 
        CHECK (available_quantity >= 0 AND reserved_quantity >= 0 AND total_quantity >= 0) ENFORCED,
    CONSTRAINT chk_product_inventories_order_quantities 
        CHECK (min_order_quantity > 0 AND (max_order_quantity IS NULL OR max_order_quantity >= min_order_quantity)) ENFORCED,
    
    -- 인덱스
    INDEX idx_product_inventories_product_id (product_id),
    INDEX idx_product_inventories_variant_id (product_variant_id),
    INDEX idx_product_inventories_location (location_code),
    INDEX idx_product_inventories_low_stock (low_stock_threshold, available_quantity),
    INDEX idx_product_inventories_active (is_active),
    INDEX idx_product_inventories_updated (updated_at)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '단순화된 상품 재고 관리 테이블 - 자체물류용';

-- 2. 재고 예약 테이블 (필수 기능만)
CREATE TABLE IF NOT EXISTS inventory_reservations
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id        VARCHAR(50) NOT NULL UNIQUE COMMENT '예약 ID',
    product_inventory_id  BIGINT NOT NULL COMMENT '재고 ID',
    
    -- 예약 정보
    reserved_quantity     INT NOT NULL COMMENT '예약 수량',
    reservation_status    ENUM('ACTIVE', 'CONFIRMED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    
    -- 참조 정보
    reference_type        VARCHAR(50) NULL COMMENT '참조 타입 (ORDER, CART)',
    reference_id          VARCHAR(100) NULL COMMENT '참조 ID',
    customer_id           BIGINT NULL COMMENT '고객 ID',
    
    -- 타이밍
    reserved_at           DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at            DATETIME(6) NOT NULL COMMENT '만료 시간',
    confirmed_at          DATETIME(6) NULL COMMENT '확정 시간',
    cancelled_at          DATETIME(6) NULL COMMENT '취소 시간',
    
    -- 메타데이터
    reason                TEXT NULL COMMENT '예약 사유',
    
    -- 외래키
    CONSTRAINT fk_inventory_reservations_inventory 
        FOREIGN KEY (product_inventory_id) REFERENCES product_inventories (id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_inventory_reservations_inventory_id (product_inventory_id),
    INDEX idx_inventory_reservations_status (reservation_status),
    INDEX idx_inventory_reservations_expires (expires_at),
    INDEX idx_inventory_reservations_reference (reference_type, reference_id)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '재고 예약 관리 테이블 - 주문 처리용';

-- 3. 통합 변경 이력 테이블 (범용)
CREATE TABLE IF NOT EXISTS entity_history
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type           VARCHAR(50) NOT NULL COMMENT '엔티티 타입 (USER, INVENTORY, ORDER)',
    entity_id             VARCHAR(100) NOT NULL COMMENT '엔티티 ID',
    
    -- 변경 정보
    change_type           VARCHAR(50) NOT NULL COMMENT '변경 타입 (CREATE, UPDATE, DELETE)',
    field_name            VARCHAR(100) NULL COMMENT '변경된 필드명',
    old_value             JSON NULL COMMENT '변경 전 값',
    new_value             JSON NULL COMMENT '변경 후 값',
    change_details        JSON NULL COMMENT '상세 변경 정보',
    
    -- 메타데이터
    changed_by_user_id    BIGINT NULL COMMENT '변경자 ID',
    changed_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address            VARCHAR(45) NULL COMMENT '변경자 IP',
    user_agent            TEXT NULL COMMENT 'User Agent',
    
    -- 인덱스
    INDEX idx_entity_history_entity (entity_type, entity_id),
    INDEX idx_entity_history_changed_at (changed_at),
    INDEX idx_entity_history_changed_by (changed_by_user_id),
    INDEX idx_entity_history_type (change_type)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '엔티티 변경 이력 통합 관리 테이블';

-- 4. 재고 알림 테이블 (단순화)
CREATE TABLE IF NOT EXISTS inventory_alerts
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_inventory_id  BIGINT NOT NULL COMMENT '재고 ID',
    
    -- 알림 정보
    alert_type            VARCHAR(100) NOT NULL COMMENT '알림 타입',
    alert_level           ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    alert_message         TEXT NOT NULL COMMENT '알림 메시지',
    
    -- 상태
    is_resolved           BOOLEAN NOT NULL DEFAULT FALSE COMMENT '해결 여부',
    resolved_at           DATETIME(6) NULL COMMENT '해결 시간',
    resolved_by_user_id   BIGINT NULL COMMENT '해결자 ID',
    
    -- 타임스탬프
    created_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- 외래키
    CONSTRAINT fk_inventory_alerts_inventory 
        FOREIGN KEY (product_inventory_id) REFERENCES product_inventories (id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_inventory_alerts_inventory_id (product_inventory_id),
    INDEX idx_inventory_alerts_type (alert_type),
    INDEX idx_inventory_alerts_level (alert_level),
    INDEX idx_inventory_alerts_resolved (is_resolved),
    INDEX idx_inventory_alerts_created (created_at)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '재고 알림 관리 테이블';

-- ============================================================================
-- 4. 주문 관리 테이블
-- ============================================================================

-- Shopping carts
CREATE TABLE shopping_carts
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT,
    session_id    VARCHAR(255),
    status        ENUM ('ACTIVE', 'ABANDONED', 'CONVERTED') NOT NULL DEFAULT 'ACTIVE',
    total_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    currency      VARCHAR(3) NOT NULL DEFAULT 'KRW',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at    DATETIME,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_shopping_carts_user_id (user_id),
    INDEX idx_shopping_carts_session_id (session_id),
    INDEX idx_shopping_carts_status (status),
    INDEX idx_shopping_carts_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Shopping cart items
CREATE TABLE shopping_cart_items
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id             BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    product_variant_id  BIGINT,
    quantity            INT NOT NULL DEFAULT 1,
    unit_price          DECIMAL(10, 2) NOT NULL,
    total_price         DECIMAL(10, 2) NOT NULL,
    added_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cart_id) REFERENCES shopping_carts (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    INDEX idx_shopping_cart_items_cart_id (cart_id),
    INDEX idx_shopping_cart_items_product_id (product_id),
    INDEX idx_shopping_cart_items_variant_id (product_variant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 5. 결제 관리 테이블
-- ============================================================================

-- Payments table (for db_payment)
CREATE TABLE payments
(
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id               VARCHAR(100) NOT NULL,                    -- Cross-domain reference (from db_order.orders)
    user_id                BIGINT       NOT NULL,                    -- Cross-domain reference (from db_platform.users)
    amount                 DECIMAL(10, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL DEFAULT 'KRW',
    status                 ENUM (
        'PENDING',
        'AUTHORIZED',
        'CAPTURED',
        'PARTIAL_CAPTURED',
        'CANCELLED',
        'FAILED',
        'REFUNDED',
        'PARTIAL_REFUNDED'
        )                                 NOT NULL DEFAULT 'PENDING',
    payment_method         VARCHAR(50)    NOT NULL,                 -- CREDIT_CARD, BANK_TRANSFER, DIGITAL_WALLET, etc.
    payment_provider       VARCHAR(50)    NOT NULL,                 -- TOSS, NICE, PAYPAL, etc.
    external_payment_id    VARCHAR(100),                            -- PG사 결제 ID
    gateway_transaction_id VARCHAR(100),                            -- PG사 거래 ID
    failure_reason         TEXT,
    
    -- 결제 세부 정보
    payment_details        JSON,                                    -- 결제 방법별 세부 정보
    
    -- 환불 관련
    parent_payment_id      BIGINT,                                  -- 원본 결제 (환불의 경우)
    refund_amount          DECIMAL(10, 2),
    refund_reason          TEXT,
    
    -- 타임스탬프
    authorized_at          DATETIME                                 NULL,
    captured_at            DATETIME                                 NULL,
    cancelled_at           DATETIME                                 NULL,
    failed_at              DATETIME                                 NULL,
    refunded_at            DATETIME                                 NULL,
    created_at             DATETIME                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME                                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Note: No foreign key constraint for cross-domain reference (order_id)
    FOREIGN KEY (parent_payment_id) REFERENCES payments (id),
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_gateway_transaction_id (gateway_transaction_id),
    INDEX idx_payments_created_at (created_at),
    INDEX idx_payments_parent_id (parent_payment_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Payment methods (결제 시점에 생성되는 결제 수단 정보)
CREATE TABLE payment_methods
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id         BIGINT      NOT NULL,               -- 결제와 1:1 관계
    user_id            BIGINT      NOT NULL,               -- Cross-domain reference (from db_platform.users)
    type               ENUM (
        'CREDIT_CARD',
        'DEBIT_CARD',
        'BANK_TRANSFER',
        'DIGITAL_WALLET',
        'CRYPTOCURRENCY',
        'VIRTUAL_ACCOUNT'
        )                          NOT NULL,
    provider           VARCHAR(50) NOT NULL,               -- VISA, MASTERCARD, PAYPAL, TOSS, NICE 등
    -- PG사로부터 받은 결제 수단 정보 (암호화되어 저장)
    gateway_method_key VARCHAR(255),                       -- PG사 결제수단 식별키
    card_last_four     VARCHAR(4),
    card_brand         VARCHAR(20),
    card_type          VARCHAR(20),                        -- 체크/신용
    bank_name          VARCHAR(100),
    account_type       VARCHAR(50),
    wallet_type        VARCHAR(50),
    -- 결제 시점 정보
    is_saved_for_reuse BOOLEAN     NOT NULL DEFAULT FALSE, -- 재사용 저장 여부
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE,
    -- Note: No foreign key constraints for cross-domain references
    INDEX idx_payment_methods_payment_id (payment_id),
    INDEX idx_payment_methods_user_id (user_id),
    INDEX idx_payment_methods_type (type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Payment transactions (for detailed transaction logging)
CREATE TABLE payment_transactions
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id       BIGINT                                          NOT NULL,
    transaction_type ENUM ('AUTHORIZE', 'CAPTURE', 'VOID', 'REFUND') NOT NULL,
    amount           DECIMAL(10, 2)                                  NOT NULL,
    currency         VARCHAR(3)                                      NOT NULL DEFAULT 'KRW',
    status           ENUM ('PENDING', 'SUCCESS', 'FAILED')           NOT NULL DEFAULT 'PENDING',
    gateway_response JSON,
    processed_at     DATETIME                                        NULL,
    created_at       DATETIME                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE,
    INDEX idx_payment_transactions_payment_id (payment_id),
    INDEX idx_payment_transactions_type (transaction_type),
    INDEX idx_payment_transactions_status (status),
    INDEX idx_payment_transactions_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 6. 알림 관리 테이블
-- ============================================================================

-- Notifications
CREATE TABLE notifications
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT,
    type          VARCHAR(50)  NOT NULL,
    title         VARCHAR(255) NOT NULL,
    message       TEXT         NOT NULL,
    data          JSON,
    is_read       BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at       DATETIME,
    is_sent       BOOLEAN      NOT NULL DEFAULT FALSE,
    sent_at       DATETIME,
    channel       VARCHAR(20)  NOT NULL DEFAULT 'WEB',
    priority      ENUM ('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL',
    expires_at    DATETIME,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_is_read (is_read),
    INDEX idx_notifications_is_sent (is_sent),
    INDEX idx_notifications_created_at (created_at),
    INDEX idx_notifications_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 7. 이벤트 저장소 (Event Sourcing)
-- ============================================================================

-- Event store for domain events
CREATE TABLE event_store
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_id      VARCHAR(255)   NOT NULL,
    aggregate_type    VARCHAR(100)   NOT NULL,
    event_type        VARCHAR(100)   NOT NULL,
    event_version     INT            NOT NULL DEFAULT 1,
    event_data        JSON           NOT NULL,
    metadata          JSON,
    occurred_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sequence_number   BIGINT         NOT NULL,
    
    UNIQUE KEY uk_event_store_aggregate_sequence (aggregate_id, sequence_number),
    INDEX idx_event_store_aggregate_id (aggregate_id),
    INDEX idx_event_store_aggregate_type (aggregate_type),
    INDEX idx_event_store_event_type (event_type),
    INDEX idx_event_store_occurred_at (occurred_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Event sourcing store for domain events';

-- ============================================================================
-- 8. 뷰 생성
-- ============================================================================

-- 재고 현황 조회용 뷰
CREATE OR REPLACE VIEW v_inventory_status AS
SELECT 
    pi.id as inventory_id,
    pi.product_id,
    p.sku as product_sku,
    p.name as product_name,
    pi.product_variant_id,
    pv.sku as variant_sku,
    pv.name as variant_name,
    pi.available_quantity,
    pi.reserved_quantity,
    pi.total_quantity,
    pi.low_stock_threshold,
    pi.location_code,
    CASE 
        WHEN pi.available_quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN pi.available_quantity <= pi.low_stock_threshold THEN 'LOW_STOCK'
        ELSE 'IN_STOCK'
    END as stock_status,
    CASE 
        WHEN pi.available_quantity = 0 THEN 0
        WHEN pi.available_quantity <= pi.low_stock_threshold THEN 25
        WHEN pi.available_quantity <= pi.reorder_point THEN 50
        ELSE 100
    END as stock_health_score,
    pi.is_active,
    pi.updated_at
FROM product_inventories pi
JOIN products p ON pi.product_id = p.id
LEFT JOIN product_variants pv ON pi.product_variant_id = pv.id
WHERE pi.is_active = TRUE;

-- ============================================================================
-- 9. 기본 데이터 초기화
-- ============================================================================

-- 기본 상품 카테고리
INSERT INTO product_categories (name, description, slug, sort_order) VALUES
('Electronics', '전자제품', 'electronics', 1),
('Fashion', '패션', 'fashion', 2),
('Home & Garden', '홈&가든', 'home-garden', 3),
('Books', '도서', 'books', 4),
('Sports', '스포츠', 'sports', 5);

-- 기본 재고 데이터 생성 (기존 상품 기준)
INSERT IGNORE INTO product_inventories (product_id, available_quantity, total_quantity)
SELECT 
    id as product_id,
    0 as available_quantity,
    0 as total_quantity
FROM products 
WHERE status = 'ACTIVE'
ON DUPLICATE KEY UPDATE product_id = product_id;

-- 변형 상품 재고 데이터 생성
INSERT IGNORE INTO product_inventories (product_id, product_variant_id, available_quantity, total_quantity)
SELECT 
    pv.product_id,
    pv.id as product_variant_id,
    0 as available_quantity,
    0 as total_quantity
FROM product_variants pv
JOIN products p ON pv.product_id = p.id
WHERE p.status = 'ACTIVE'
ON DUPLICATE KEY UPDATE product_id = product_id;

-- ============================================================================
-- 10. 스키마 검증
-- ============================================================================

-- 재고 일관성 검증
SELECT 'Inventory Consistency Check' as check_type,
       COUNT(*) as total_inventories,
       COUNT(CASE WHEN total_quantity != (available_quantity + reserved_quantity) THEN 1 END) as inconsistent_count
FROM product_inventories;

-- 기본 재고 현황
SELECT 
    stock_status,
    COUNT(*) as count
FROM v_inventory_status
GROUP BY stock_status;

SELECT '✅ 자체물류용 단순화된 이커머스 스키마 생성 완료' as status;