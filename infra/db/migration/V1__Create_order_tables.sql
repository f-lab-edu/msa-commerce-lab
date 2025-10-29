-- =====================================================
-- Order Service Database Schema Migration
-- Version: V1
-- Description: Migrate orders table from V0 schema to UUID-based PK
-- =====================================================

USE db_order;

-- ===================================================
-- STEP 1: order_items 테이블 FK 제약조건 제거 (존재하는 경우)
-- ===================================================
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'db_order'
      AND TABLE_NAME = 'order_items'
      AND CONSTRAINT_NAME = 'order_items_ibfk_1'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @drop_fk_sql = IF(@fk_exists > 0,
    'ALTER TABLE order_items DROP FOREIGN KEY order_items_ibfk_1',
    'SELECT "FK constraint does not exist, skipping..." AS info'
);

PREPARE stmt FROM @drop_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================================================
-- STEP 2: orders 테이블 스키마 변경
-- ===================================================

-- 2-1. 기존 PK 및 인덱스 제거
-- AUTO_INCREMENT 속성 제거 (PK 제거 전 필수)
ALTER TABLE orders MODIFY COLUMN id BIGINT NOT NULL;

-- UNIQUE 제약조건 제거 (PK 제거 전 필요)
ALTER TABLE orders DROP INDEX order_uuid;
ALTER TABLE orders DROP INDEX order_number;

-- PK 제거
ALTER TABLE orders DROP PRIMARY KEY;

-- 인덱스 제거 (조건부 처리)
-- idx_orders_uuid 제거
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'db_order'
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_orders_uuid'
);
SET @drop_index_sql = IF(@index_exists > 0,
    'ALTER TABLE orders DROP INDEX idx_orders_uuid',
    'SELECT "Index idx_orders_uuid does not exist" AS info'
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- idx_orders_user_id 제거
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'db_order'
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_orders_user_id'
);
SET @drop_index_sql = IF(@index_exists > 0,
    'ALTER TABLE orders DROP INDEX idx_orders_user_id',
    'SELECT "Index idx_orders_user_id does not exist" AS info'
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- idx_orders_status 제거
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'db_order'
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_orders_status'
);
SET @drop_index_sql = IF(@index_exists > 0,
    'ALTER TABLE orders DROP INDEX idx_orders_status',
    'SELECT "Index idx_orders_status does not exist" AS info'
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- idx_orders_date 제거
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'db_order'
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_orders_date'
);
SET @drop_index_sql = IF(@index_exists > 0,
    'ALTER TABLE orders DROP INDEX idx_orders_date',
    'SELECT "Index idx_orders_date does not exist" AS info'
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2-2. 컬럼 변경
ALTER TABLE orders
    -- id 컬럼 제거
    DROP COLUMN id,

    -- 컬럼명 변경: order_uuid -> order_id (PK로 사용)
    CHANGE COLUMN order_uuid order_id CHAR(36) NOT NULL COMMENT '주문 UUID',

    -- 컬럼명 변경: user_id -> customer_id
    CHANGE COLUMN user_id customer_id BIGINT NOT NULL COMMENT '고객 ID',

    -- status 컬럼 타입 변경: ENUM -> VARCHAR(20)
    MODIFY COLUMN status VARCHAR(20) NOT NULL COMMENT '주문 상태',

    -- 타임스탬프 컬럼 타입 변경: TIMESTAMP -> DATETIME(6)
    MODIFY COLUMN order_date DATETIME(6) NOT NULL COMMENT '주문 일시',
    MODIFY COLUMN confirmed_at DATETIME(6) NULL COMMENT '주문 확정 일시',
    MODIFY COLUMN payment_completed_at DATETIME(6) NULL COMMENT '결제 완료 일시',
    MODIFY COLUMN shipped_at DATETIME(6) NULL COMMENT '배송 시작 일시',
    MODIFY COLUMN delivered_at DATETIME(6) NULL COMMENT '배송 완료 일시',
    MODIFY COLUMN cancelled_at DATETIME(6) NULL COMMENT '주문 취소 일시',
    MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 일시';

-- 2-3. order_id를 PK로 설정
ALTER TABLE orders
    ADD PRIMARY KEY (order_id);

-- 2-4. 인덱스 재구성
-- 복합 인덱스(composite index) 우선 설계: 단일 컬럼 인덱스는 복합 인덱스로 대체 가능한 경우 제거
-- 데이터 접근 패턴: customer_id + status 조합 조회가 빈번하므로 복합 인덱스 우선
ALTER TABLE orders
    ADD INDEX idx_order_number (order_number),
    -- customer_id 단독 인덱스 제거: idx_customer_status 복합 인덱스가 customer_id 조회도 커버 가능
    -- status 단독 인덱스 제거: 카디널리티가 낮아 풀 스캔 대비 성능 이점 제한적
    ADD INDEX idx_customer_status (customer_id, status),
    ADD INDEX idx_order_date (order_date),
    ADD INDEX idx_created_at (created_at);

-- ===================================================
-- STEP 3: order_items 테이블 스키마 변경
-- ===================================================

-- 3-1. order_item_id 컬럼 추가 (NULL 허용)
ALTER TABLE order_items
    ADD COLUMN order_item_id VARCHAR(36) NULL COMMENT '주문 항목 UUID' AFTER id;

-- 3-2. 기존 데이터에 대한 order_item_id UUID 생성
UPDATE order_items SET order_item_id = UUID();

-- 3-3. 기존 PK 및 인덱스 제거
-- AUTO_INCREMENT 속성 제거 (PK 제거 전 필수)
ALTER TABLE order_items MODIFY COLUMN id BIGINT NOT NULL;

-- PK 제거
ALTER TABLE order_items DROP PRIMARY KEY;

-- 인덱스 제거 (조건부 처리)
-- idx_order_items_order_id 제거
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'db_order'
      AND TABLE_NAME = 'order_items'
      AND INDEX_NAME = 'idx_order_items_order_id'
);
SET @drop_index_sql = IF(@index_exists > 0,
    'ALTER TABLE order_items DROP INDEX idx_order_items_order_id',
    'SELECT "Index idx_order_items_order_id does not exist" AS info'
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- idx_order_items_product_id 제거
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'db_order'
      AND TABLE_NAME = 'order_items'
      AND INDEX_NAME = 'idx_order_items_product_id'
);
SET @drop_index_sql = IF(@index_exists > 0,
    'ALTER TABLE order_items DROP INDEX idx_order_items_product_id',
    'SELECT "Index idx_order_items_product_id does not exist" AS info'
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3-4. 컬럼 변경
ALTER TABLE order_items
    -- id 컬럼 제거
    DROP COLUMN id,

    -- order_id FK 타입 변경 (BIGINT → CHAR(36))
    MODIFY COLUMN order_id CHAR(36) NOT NULL COMMENT '주문 UUID',

    -- order_item_id를 PK로 변경
    MODIFY COLUMN order_item_id CHAR(36) NOT NULL COMMENT '주문 항목 UUID',

    -- 타임스탬프 컬럼 타입 변경
    MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시';

-- 3-5. order_item_id를 PK로 설정
ALTER TABLE order_items
    ADD PRIMARY KEY (order_item_id);

-- 3-6. 인덱스 재구성
ALTER TABLE order_items
    ADD INDEX idx_order_id (order_id),
    ADD INDEX idx_product_id (product_id),
    ADD INDEX idx_created_at (created_at);

-- 3-7. FK 제약조건 재생성
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE;
