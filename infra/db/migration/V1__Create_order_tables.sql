-- =====================================================
-- Order Service Database Schema Migration
-- Version: V1
-- Description: Migrate orders table from V0 schema
-- =====================================================

USE db_order;

-- orders 테이블 스키마 변경
ALTER TABLE orders
    -- 컬럼명 변경: order_uuid -> order_id
    CHANGE COLUMN order_uuid order_id CHAR(36) NOT NULL UNIQUE COMMENT '주문 UUID',

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

-- 인덱스 재구성 (기존 인덱스 삭제 후 재생성)
ALTER TABLE orders
    DROP INDEX idx_orders_uuid,
    DROP INDEX idx_orders_user_id,
    DROP INDEX idx_orders_status,
    DROP INDEX idx_orders_date,
    ADD INDEX idx_order_id (order_id),
    ADD INDEX idx_order_number (order_number),
    ADD INDEX idx_customer_status (customer_id, status),
    ADD INDEX idx_order_date (order_date),
    ADD INDEX idx_created_at (created_at);

-- order_items 테이블에 order_item_id 컬럼 추가
ALTER TABLE order_items
    ADD COLUMN order_item_id VARCHAR(36) NOT NULL UNIQUE COMMENT '주문 항목 UUID' AFTER id,

    -- 타임스탬프 컬럼 타입 변경
    MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시';

-- order_items 인덱스 재구성
ALTER TABLE order_items
    DROP INDEX idx_order_items_order_id,
    DROP INDEX idx_order_items_product_id,
    ADD INDEX idx_order_item_id (order_item_id),
    ADD INDEX idx_order_id (order_id),
    ADD INDEX idx_product_id (product_id),
    ADD INDEX idx_created_at (created_at);

-- 기존 데이터에 대한 order_item_id UUID 생성 (기존 데이터가 있는 경우)
UPDATE order_items SET order_item_id = UUID() WHERE order_item_id IS NULL OR order_item_id = '';
