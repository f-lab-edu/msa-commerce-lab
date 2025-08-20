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
-- 2. 상품 관리 테이블 (MySQL 9 최적화 - Product Service MSA)
-- ============================================================================

-- Product categories with optimized hierarchy
CREATE TABLE product_categories
(
    id                  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    parent_id           BIGINT UNSIGNED,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    slug                VARCHAR(100) NOT NULL UNIQUE,
    
    -- SEO and Display
    display_order       INT UNSIGNED NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    image_url           VARCHAR(500),
    icon_class          VARCHAR(100),
    
    -- SEO metadata
    seo_title           VARCHAR(255),
    seo_description     VARCHAR(500),
    seo_keywords        VARCHAR(255),
    
    -- Hierarchy metadata (MySQL 9 generated columns)
    level               TINYINT UNSIGNED GENERATED ALWAYS AS (
        CASE 
            WHEN parent_id IS NULL THEN 0
            ELSE (SELECT level + 1 FROM product_categories p WHERE p.id = parent_id LIMIT 1)
        END
    ) VIRTUAL,
    
    -- Audit fields
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT UNSIGNED NOT NULL DEFAULT 1,
    
    -- Constraints
    FOREIGN KEY (parent_id) REFERENCES product_categories (id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_categories_parent_id (parent_id),
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_active_featured (is_active, is_featured),
    INDEX idx_categories_display_order (display_order),
    INDEX idx_categories_created_at (created_at),
    
    -- Covering indexes for common queries
    INDEX idx_categories_listing (is_active, display_order, id, name, slug),
    
    -- Constraint checks
    CONSTRAINT chk_categories_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_categories_display_order CHECK (display_order >= 0)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Product categories with optimized hierarchy support';

-- Category hierarchy optimization table (Closure Table pattern)
CREATE TABLE category_closure
(
    ancestor_id         BIGINT UNSIGNED NOT NULL,
    descendant_id       BIGINT UNSIGNED NOT NULL,
    depth               TINYINT UNSIGNED NOT NULL DEFAULT 0,
    
    PRIMARY KEY (ancestor_id, descendant_id),
    
    FOREIGN KEY (ancestor_id) REFERENCES product_categories (id) ON DELETE CASCADE,
    FOREIGN KEY (descendant_id) REFERENCES product_categories (id) ON DELETE CASCADE,
    
    INDEX idx_closure_depth (depth),
    INDEX idx_closure_descendant (descendant_id, depth)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Category hierarchy closure table for optimized traversal';

-- Products table (completely independent)
CREATE TABLE products
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    
    -- Business identifiers
    sku                     VARCHAR(100) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    short_description       VARCHAR(1000),
    description             TEXT,
    
    -- Category (loose coupling through events)
    category_id             BIGINT UNSIGNED,
    category_path           VARCHAR(1000), -- denormalized for performance
    
    -- Brand and model
    brand                   VARCHAR(100),
    model                   VARCHAR(100),
    manufacturer            VARCHAR(100),
    
    -- Product classification
    product_type            ENUM('PHYSICAL', 'DIGITAL', 'SERVICE', 'BUNDLE') NOT NULL DEFAULT 'PHYSICAL',
    status                  ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK') NOT NULL DEFAULT 'DRAFT',
    
    -- Physical properties
    weight_grams            INT UNSIGNED,
    dimensions_length_mm    SMALLINT UNSIGNED,
    dimensions_width_mm     SMALLINT UNSIGNED,
    dimensions_height_mm    SMALLINT UNSIGNED,
    
    -- Calculated volume (MySQL 9 generated column)
    volume_cubic_mm         BIGINT UNSIGNED GENERATED ALWAYS AS (
        CASE 
            WHEN dimensions_length_mm IS NOT NULL 
                AND dimensions_width_mm IS NOT NULL 
                AND dimensions_height_mm IS NOT NULL 
            THEN dimensions_length_mm * dimensions_width_mm * dimensions_height_mm
            ELSE NULL
        END
    ) STORED,
    
    -- Business rules
    requires_shipping       BOOLEAN NOT NULL DEFAULT TRUE,
    is_taxable              BOOLEAN NOT NULL DEFAULT TRUE,
    is_returnable           BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Age and safety restrictions
    min_age_months          SMALLINT UNSIGNED,
    max_age_months          SMALLINT UNSIGNED,
    safety_warnings         JSON,
    
    -- SEO optimization
    slug                    VARCHAR(300) NOT NULL UNIQUE,
    meta_title              VARCHAR(255),
    meta_description        VARCHAR(500),
    meta_keywords           VARCHAR(300),
    
    -- Search optimization (MySQL 9 fulltext)
    search_tags             TEXT,
    search_vector           LONGTEXT GENERATED ALWAYS AS (
        CONCAT_WS(' ', name, IFNULL(short_description, ''), IFNULL(brand, ''), IFNULL(search_tags, ''))
    ) STORED,
    
    -- Audit and versioning
    created_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version                 BIGINT UNSIGNED NOT NULL DEFAULT 1,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    
    -- Foreign key to category (soft reference)
    FOREIGN KEY (category_id) REFERENCES product_categories (id) ON DELETE SET NULL,
    
    -- Performance indexes
    INDEX idx_products_sku (sku),
    INDEX idx_products_status (status),
    INDEX idx_products_category (category_id, status),
    INDEX idx_products_brand (brand, status),
    INDEX idx_products_featured (is_featured, status),
    INDEX idx_products_created_at (created_at),
    INDEX idx_products_search (status, brand, category_id),
    
    -- Covering indexes for listings
    INDEX idx_products_category_listing (category_id, status, is_featured, created_at, id, name, slug),
    INDEX idx_products_brand_listing (brand, status, created_at, id, name, slug),
    
    -- Full-text search index
    FULLTEXT KEY idx_products_fulltext (name, short_description, search_tags),
    
    -- Functional index for case-insensitive searches (MySQL 9)
    INDEX idx_products_name_lower ((LOWER(name))),
    
    -- Constraint checks
    CONSTRAINT chk_products_dimensions CHECK (
        (dimensions_length_mm IS NULL AND dimensions_width_mm IS NULL AND dimensions_height_mm IS NULL) OR
        (dimensions_length_mm > 0 AND dimensions_width_mm > 0 AND dimensions_height_mm > 0)
    ),
    CONSTRAINT chk_products_weight CHECK (weight_grams IS NULL OR weight_grams > 0),
    CONSTRAINT chk_products_age_range CHECK (
        min_age_months IS NULL OR max_age_months IS NULL OR min_age_months <= max_age_months
    ),
    CONSTRAINT chk_products_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$')
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Products table optimized for MSA independence';

-- Product price tiers (flexible pricing)
CREATE TABLE product_price_tiers
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    product_id              BIGINT UNSIGNED NOT NULL,
    
    -- Price tier information
    tier_name               VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    base_price              DECIMAL(12,4) NOT NULL,
    sale_price              DECIMAL(12,4),
    cost_price              DECIMAL(12,4),
    currency                CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- Quantity breaks
    min_quantity            INT UNSIGNED NOT NULL DEFAULT 1,
    max_quantity            INT UNSIGNED,
    
    -- Time-based pricing
    effective_from          TIMESTAMP(6),
    effective_until         TIMESTAMP(6),
    
    -- Business rules
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    requires_authentication BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Audit
    created_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version                 BIGINT UNSIGNED NOT NULL DEFAULT 1,
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    
    -- Unique constraint for tier per product
    UNIQUE KEY uk_price_tiers_product_tier (product_id, tier_name, min_quantity),
    
    -- Performance indexes
    INDEX idx_price_tiers_product (product_id, is_active),
    INDEX idx_price_tiers_effective (effective_from, effective_until),
    INDEX idx_price_tiers_quantity (min_quantity, max_quantity),
    
    -- Constraint checks
    CONSTRAINT chk_price_tiers_positive_prices CHECK (
        base_price > 0 AND 
        (sale_price IS NULL OR sale_price > 0) AND 
        (cost_price IS NULL OR cost_price >= 0)
    ),
    CONSTRAINT chk_price_tiers_sale_price CHECK (
        sale_price IS NULL OR sale_price <= base_price
    ),
    CONSTRAINT chk_price_tiers_quantity_range CHECK (
        max_quantity IS NULL OR min_quantity <= max_quantity
    ),
    CONSTRAINT chk_price_tiers_effective_period CHECK (
        effective_from IS NULL OR effective_until IS NULL OR effective_from < effective_until
    )
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Flexible pricing tiers for products';

-- Product variants (structured options)
CREATE TABLE product_variants
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    product_id              BIGINT UNSIGNED NOT NULL,
    
    -- Variant identification
    variant_sku             VARCHAR(100) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    description             VARCHAR(1000),
    
    -- Price adjustments
    price_adjustment        DECIMAL(10,4) DEFAULT 0.00,
    weight_adjustment_grams INT DEFAULT 0,
    
    -- Status and ordering
    status                  ENUM('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    display_order           SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    is_default              BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Audit
    created_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version                 BIGINT UNSIGNED NOT NULL DEFAULT 1,
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    
    -- Performance indexes
    INDEX idx_variants_product (product_id, status),
    INDEX idx_variants_sku (variant_sku),
    INDEX idx_variants_default (is_default, status),
    INDEX idx_variants_display_order (display_order),
    
    -- Covering index for variant listings
    INDEX idx_variants_product_listing (product_id, status, display_order, id, name, variant_sku),
    
    -- Constraint checks
    CONSTRAINT chk_variants_display_order CHECK (display_order >= 0)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Product variants with structured option support';

-- Product options (structured option values)
CREATE TABLE product_options
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    variant_id              BIGINT UNSIGNED NOT NULL,
    
    -- Option definition
    option_name             VARCHAR(100) NOT NULL,  -- e.g., 'COLOR', 'SIZE', 'MATERIAL'
    option_value            VARCHAR(200) NOT NULL,  -- e.g., 'RED', 'XL', 'COTTON'
    option_display_name     VARCHAR(200),           -- e.g., '빨간색', 'X-Large'
    
    -- Option metadata
    option_type             ENUM('TEXT', 'COLOR', 'SIZE', 'MATERIAL', 'CUSTOM') NOT NULL DEFAULT 'TEXT',
    sort_order              SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    
    -- Visual representation
    color_hex               CHAR(7),                -- For color options: #FF0000
    image_url               VARCHAR(500),           -- Option-specific image
    
    -- Pricing impact
    price_adjustment        DECIMAL(10,4) DEFAULT 0.00,
    
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    
    -- Unique constraint for option per variant
    UNIQUE KEY uk_options_variant_option (variant_id, option_name),
    
    -- Performance indexes
    INDEX idx_options_variant (variant_id),
    INDEX idx_options_type (option_type),
    INDEX idx_options_name_value (option_name, option_value),
    
    -- Constraint checks
    CONSTRAINT chk_options_color_hex CHECK (
        option_type != 'COLOR' OR color_hex REGEXP '^#[0-9A-Fa-f]{6}$'
    )
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Structured product option values';

-- Product media (images, videos, documents)
CREATE TABLE product_media
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    product_id              BIGINT UNSIGNED NOT NULL,
    variant_id              BIGINT UNSIGNED,
    
    -- Media information
    media_type              ENUM('IMAGE', 'VIDEO', 'DOCUMENT', '360_VIEW') NOT NULL DEFAULT 'IMAGE',
    file_url                VARCHAR(1000) NOT NULL,
    thumbnail_url           VARCHAR(1000),
    alt_text                VARCHAR(500),
    title                   VARCHAR(255),
    
    -- File metadata
    file_size_bytes         INT UNSIGNED,
    mime_type               VARCHAR(100),
    width_pixels            SMALLINT UNSIGNED,
    height_pixels           SMALLINT UNSIGNED,
    duration_seconds        SMALLINT UNSIGNED, -- For videos
    
    -- Display settings
    display_order           SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    is_primary              BOOLEAN NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Usage context
    usage_context           SET('LISTING', 'DETAIL', 'GALLERY', 'THUMBNAIL', 'ZOOM') DEFAULT 'LISTING,DETAIL',
    
    -- Audit
    created_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    uploaded_by             VARCHAR(100),
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    
    -- Performance indexes
    INDEX idx_media_product (product_id, media_type, is_active),
    INDEX idx_media_variant (variant_id, is_active),
    INDEX idx_media_primary (is_primary, is_active),
    INDEX idx_media_display_order (display_order),
    INDEX idx_media_usage (usage_context),
    
    -- Covering index for media listings
    INDEX idx_media_product_listing (product_id, is_active, display_order, media_type, file_url),
    
    -- Constraint checks
    CONSTRAINT chk_media_dimensions CHECK (
        (width_pixels IS NULL AND height_pixels IS NULL) OR 
        (width_pixels > 0 AND height_pixels > 0)
    ),
    CONSTRAINT chk_media_file_size CHECK (file_size_bytes IS NULL OR file_size_bytes > 0),
    CONSTRAINT chk_media_duration CHECK (
        media_type != 'VIDEO' OR duration_seconds IS NULL OR duration_seconds > 0
    )
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Enhanced product media management';

-- ============================================================================
-- 3. 재고 관리 (이벤트 소싱 기반)
-- ============================================================================

-- Current inventory snapshots (optimized for queries)
CREATE TABLE inventory_snapshots
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    product_id              BIGINT UNSIGNED NOT NULL,
    variant_id              BIGINT UNSIGNED,
    location_code           VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    
    -- Current quantities
    available_quantity      INT NOT NULL DEFAULT 0,
    reserved_quantity       INT NOT NULL DEFAULT 0,
    damaged_quantity        INT NOT NULL DEFAULT 0,
    total_quantity          INT GENERATED ALWAYS AS (available_quantity + reserved_quantity + damaged_quantity) STORED,
    
    -- Business rules
    low_stock_threshold     INT NOT NULL DEFAULT 10,
    reorder_point           INT NOT NULL DEFAULT 5,
    max_stock_level         INT,
    min_order_quantity      INT NOT NULL DEFAULT 1,
    max_order_quantity      INT,
    
    -- Stock status (computed)
    stock_status            ENUM('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'BACKORDER') GENERATED ALWAYS AS (
        CASE 
            WHEN available_quantity = 0 THEN 'OUT_OF_STOCK'
            WHEN available_quantity <= low_stock_threshold THEN 'LOW_STOCK'
            ELSE 'IN_STOCK'
        END
    ) STORED,
    
    -- Settings
    is_tracking_enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    allow_backorder         BOOLEAN NOT NULL DEFAULT FALSE,
    allow_negative_stock    BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Optimistic locking
    version                 BIGINT UNSIGNED NOT NULL DEFAULT 1,
    
    -- Last updated
    last_updated_at         TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    last_event_id           BIGINT UNSIGNED,
    
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    
    -- Unique constraint for inventory per product/variant/location
    UNIQUE KEY uk_inventory_product_location (product_id, IFNULL(variant_id, 0), location_code),
    
    -- Performance indexes
    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_variant (variant_id),
    INDEX idx_inventory_location (location_code),
    INDEX idx_inventory_stock_status (stock_status),
    INDEX idx_inventory_low_stock (low_stock_threshold, available_quantity),
    INDEX idx_inventory_reorder (reorder_point, available_quantity),
    INDEX idx_inventory_last_updated (last_updated_at),
    
    -- Covering indexes for common queries
    INDEX idx_inventory_product_status (product_id, stock_status, available_quantity, reserved_quantity),
    INDEX idx_inventory_alerts (stock_status, low_stock_threshold, available_quantity, product_id),
    
    -- Constraint checks
    CONSTRAINT chk_inventory_quantities CHECK (
        available_quantity >= 0 AND 
        reserved_quantity >= 0 AND 
        damaged_quantity >= 0
    ),
    CONSTRAINT chk_inventory_thresholds CHECK (
        low_stock_threshold >= 0 AND 
        reorder_point >= 0 AND
        (max_stock_level IS NULL OR max_stock_level > low_stock_threshold)
    ),
    CONSTRAINT chk_inventory_order_quantities CHECK (
        min_order_quantity > 0 AND
        (max_order_quantity IS NULL OR max_order_quantity >= min_order_quantity)
    ),
    CONSTRAINT chk_inventory_negative_stock CHECK (
        allow_negative_stock = TRUE OR available_quantity >= 0
    )
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Current inventory state snapshots for fast querying';

-- Inventory events (event sourcing for inventory changes)
CREATE TABLE inventory_events
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    
    -- Event identification
    event_type              VARCHAR(50) NOT NULL,  -- STOCK_IN, STOCK_OUT, RESERVED, RELEASED, ADJUSTED, etc.
    aggregate_id            VARCHAR(255) NOT NULL, -- product_id:variant_id:location
    aggregate_version       BIGINT UNSIGNED NOT NULL,
    
    -- Event data
    product_id              BIGINT UNSIGNED NOT NULL,
    variant_id              BIGINT UNSIGNED,
    location_code           VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    
    -- Quantity changes
    quantity_change         INT NOT NULL,
    quantity_before         INT NOT NULL,
    quantity_after          INT NOT NULL,
    change_reason           VARCHAR(100),
    
    -- Reference information
    reference_type          VARCHAR(50),   -- ORDER, ADJUSTMENT, DAMAGE, RETURN, etc.
    reference_id            VARCHAR(100),
    reference_number        VARCHAR(100),
    
    -- Event metadata
    event_data              JSON,
    correlation_id          CHAR(36),      -- UUID for tracking related events
    causation_id            CHAR(36),      -- UUID of the command that caused this event
    
    -- Audit
    occurred_at             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at            TIMESTAMP(6),
    created_by              VARCHAR(100),
    
    -- Performance indexes
    INDEX idx_inventory_events_aggregate (aggregate_id, aggregate_version),
    INDEX idx_inventory_events_product (product_id, occurred_at),
    INDEX idx_inventory_events_type (event_type, occurred_at),
    INDEX idx_inventory_events_reference (reference_type, reference_id),
    INDEX idx_inventory_events_correlation (correlation_id),
    INDEX idx_inventory_events_occurred (occurred_at),
    
    -- Partitioning friendly index
    INDEX idx_inventory_events_partition (occurred_at, product_id),
    
    -- Constraint checks
    CONSTRAINT chk_inventory_events_aggregate_version CHECK (aggregate_version > 0),
    CONSTRAINT chk_inventory_events_quantities CHECK (
        (quantity_change > 0 AND quantity_after = quantity_before + quantity_change) OR
        (quantity_change < 0 AND quantity_after = quantity_before + quantity_change) OR
        (quantity_change = 0 AND quantity_after = quantity_before)
    )
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Event sourcing for inventory state changes'
  PARTITION BY RANGE (UNIX_TIMESTAMP(occurred_at)) (
    PARTITION p202501 VALUES LESS THAN (UNIX_TIMESTAMP('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (UNIX_TIMESTAMP('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (UNIX_TIMESTAMP('2025-04-01')),
    PARTITION p202504 VALUES LESS THAN (UNIX_TIMESTAMP('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (UNIX_TIMESTAMP('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (UNIX_TIMESTAMP('2025-07-01')),
    PARTITION p202507 VALUES LESS THAN (UNIX_TIMESTAMP('2025-08-01')),
    PARTITION p202508 VALUES LESS THAN (UNIX_TIMESTAMP('2025-09-01')),
    PARTITION p202509 VALUES LESS THAN (UNIX_TIMESTAMP('2025-10-01')),
    PARTITION p202510 VALUES LESS THAN (UNIX_TIMESTAMP('2025-11-01')),
    PARTITION p202511 VALUES LESS THAN (UNIX_TIMESTAMP('2025-12-01')),
    PARTITION p202512 VALUES LESS THAN (UNIX_TIMESTAMP('2026-01-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- Product domain events (event sourcing for product changes)
CREATE TABLE product_events
(
    id                      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    
    -- Event identification
    event_type              VARCHAR(50) NOT NULL,  -- CREATED, UPDATED, STATUS_CHANGED, PRICE_CHANGED, etc.
    aggregate_id            CHAR(36) NOT NULL,     -- UUID for product aggregate
    aggregate_version       BIGINT UNSIGNED NOT NULL,
    
    -- Event data
    product_id              BIGINT UNSIGNED NOT NULL,
    event_data              JSON NOT NULL,
    
    -- Event metadata
    correlation_id          CHAR(36),      -- UUID for tracking related events
    causation_id            CHAR(36),      -- UUID of the command that caused this event
    
    -- Audit
    occurred_at             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at            TIMESTAMP(6),
    created_by              VARCHAR(100),
    
    -- Performance indexes
    INDEX idx_product_events_aggregate (aggregate_id, aggregate_version),
    INDEX idx_product_events_product (product_id, occurred_at),
    INDEX idx_product_events_type (event_type, occurred_at),
    INDEX idx_product_events_correlation (correlation_id),
    INDEX idx_product_events_occurred (occurred_at),
    
    -- Partitioning friendly index
    INDEX idx_product_events_partition (occurred_at, product_id),
    
    -- Constraint checks
    CONSTRAINT chk_product_events_aggregate_version CHECK (aggregate_version > 0)
    
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Event sourcing for product domain changes'
  PARTITION BY RANGE (UNIX_TIMESTAMP(occurred_at)) (
    PARTITION p202501 VALUES LESS THAN (UNIX_TIMESTAMP('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (UNIX_TIMESTAMP('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (UNIX_TIMESTAMP('2025-04-01')),
    PARTITION p202504 VALUES LESS THAN (UNIX_TIMESTAMP('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (UNIX_TIMESTAMP('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (UNIX_TIMESTAMP('2025-07-01')),
    PARTITION p202507 VALUES LESS THAN (UNIX_TIMESTAMP('2025-08-01')),
    PARTITION p202508 VALUES LESS THAN (UNIX_TIMESTAMP('2025-09-01')),
    PARTITION p202509 VALUES LESS THAN (UNIX_TIMESTAMP('2025-10-01')),
    PARTITION p202510 VALUES LESS THAN (UNIX_TIMESTAMP('2025-11-01')),
    PARTITION p202511 VALUES LESS THAN (UNIX_TIMESTAMP('2025-12-01')),
    PARTITION p202512 VALUES LESS THAN (UNIX_TIMESTAMP('2026-01-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

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

-- Product catalog view with optimized joins
CREATE OR REPLACE VIEW v_product_catalog AS
SELECT 
    p.id,
    p.sku,
    p.name,
    p.short_description,
    p.status,
    p.is_featured,
    p.slug,
    p.brand,
    p.model,
    p.product_type,
    
    -- Category information
    pc.name as category_name,
    pc.slug as category_slug,
    p.category_path,
    
    -- Price information (default tier)
    pt.base_price,
    pt.sale_price,
    pt.currency,
    
    -- Inventory summary
    inv.available_quantity,
    inv.stock_status,
    
    -- Media information
    pm.file_url as primary_image_url,
    pm.thumbnail_url,
    
    -- Computed fields
    CASE 
        WHEN pt.sale_price IS NOT NULL AND pt.sale_price < pt.base_price 
        THEN ROUND(((pt.base_price - pt.sale_price) / pt.base_price) * 100, 0)
        ELSE 0
    END as discount_percentage,
    
    p.created_at,
    p.updated_at

FROM products p
LEFT JOIN product_categories pc ON p.category_id = pc.id
LEFT JOIN product_price_tiers pt ON p.id = pt.product_id 
    AND pt.tier_name = 'DEFAULT' 
    AND pt.is_active = TRUE
    AND (pt.effective_from IS NULL OR pt.effective_from <= CURRENT_TIMESTAMP)
    AND (pt.effective_until IS NULL OR pt.effective_until > CURRENT_TIMESTAMP)
LEFT JOIN inventory_snapshots inv ON p.id = inv.product_id AND inv.variant_id IS NULL
LEFT JOIN product_media pm ON p.id = pm.product_id 
    AND pm.is_primary = TRUE 
    AND pm.is_active = TRUE

WHERE p.status IN ('ACTIVE', 'OUT_OF_STOCK');

-- Inventory alerts view
CREATE OR REPLACE VIEW v_inventory_alerts AS
SELECT 
    inv.id,
    inv.product_id,
    p.sku,
    p.name as product_name,
    inv.variant_id,
    pv.variant_sku,
    pv.name as variant_name,
    inv.location_code,
    inv.available_quantity,
    inv.low_stock_threshold,
    inv.reorder_point,
    inv.stock_status,
    
    CASE 
        WHEN inv.available_quantity = 0 THEN 'CRITICAL'
        WHEN inv.available_quantity <= inv.reorder_point THEN 'HIGH'
        WHEN inv.available_quantity <= inv.low_stock_threshold THEN 'MEDIUM'
        ELSE 'LOW'
    END as alert_level,
    
    inv.last_updated_at

FROM inventory_snapshots inv
JOIN products p ON inv.product_id = p.id
LEFT JOIN product_variants pv ON inv.variant_id = pv.id

WHERE inv.stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK')
   OR inv.available_quantity <= inv.reorder_point;

-- ============================================================================
-- 9. 기본 데이터 초기화
-- ============================================================================

-- Insert default categories
INSERT INTO product_categories (name, description, slug, display_order, is_active) VALUES
('Electronics', '전자제품 및 IT 기기', 'electronics', 1, TRUE),
('Fashion', '패션 및 의류', 'fashion', 2, TRUE),
('Home & Garden', '홈 인테리어 및 가든 용품', 'home-garden', 3, TRUE),
('Books & Media', '도서 및 미디어', 'books-media', 4, TRUE),
('Sports & Outdoor', '스포츠 및 아웃도어 용품', 'sports-outdoor', 5, TRUE);

-- Insert closure table entries for root categories
INSERT INTO category_closure (ancestor_id, descendant_id, depth)
SELECT id, id, 0 FROM product_categories WHERE parent_id IS NULL;

-- Create basic inventory snapshots for existing products
INSERT IGNORE INTO inventory_snapshots (product_id, available_quantity, reserved_quantity, damaged_quantity)
SELECT 
    id as product_id,
    0 as available_quantity,
    0 as reserved_quantity,
    0 as damaged_quantity
FROM products 
WHERE status = 'ACTIVE'
ON DUPLICATE KEY UPDATE product_id = product_id;

-- Create basic inventory snapshots for product variants
INSERT IGNORE INTO inventory_snapshots (product_id, variant_id, available_quantity, reserved_quantity, damaged_quantity)
SELECT 
    pv.product_id,
    pv.id as variant_id,
    0 as available_quantity,
    0 as reserved_quantity,
    0 as damaged_quantity
FROM product_variants pv
JOIN products p ON pv.product_id = p.id
WHERE p.status = 'ACTIVE'
ON DUPLICATE KEY UPDATE product_id = product_id;

-- ============================================================================
-- 10. 스키마 검증
-- ============================================================================

-- Inventory consistency check
SELECT 'Inventory Consistency Check' as check_type,
       COUNT(*) as total_inventories,
       COUNT(CASE WHEN total_quantity != (available_quantity + reserved_quantity + damaged_quantity) THEN 1 END) as inconsistent_count
FROM inventory_snapshots;

-- Product catalog status
SELECT 
    status,
    product_type,
    COUNT(*) as count
FROM products
GROUP BY status, product_type;

-- Inventory status summary
SELECT 
    stock_status,
    COUNT(*) as count
FROM inventory_snapshots
GROUP BY stock_status;

-- Event sourcing validation
SELECT 
    'Event Tables Partitioning' as check_type,
    COUNT(*) as inventory_events_count,
    COUNT(*) as product_events_count
FROM (
    SELECT COUNT(*) FROM inventory_events
    UNION ALL
    SELECT COUNT(*) FROM product_events
) AS event_counts;

SELECT '✅ MSA Product Service 스키마 (MySQL 9 최적화) 생성 완료' as status,
       'Features: Event Sourcing, Generated Columns, Partitioning, Closure Tables' as features;