-- =====================================================
-- Payment Service Database Schema Migration
-- Version: V2
-- Description: order_id 를 UUID(CHAR(36))로 정렬하고, 주문당 활성 결제 1건 제약을 추가
-- =====================================================

USE db_payment;

-- ===================================================
-- STEP 1: order_id 타입 정렬
-- V1 에서 db_order.orders 의 PK 가 CHAR(36) UUID 로 바뀌었으므로
-- cross-domain 참조 컬럼도 같은 타입으로 맞춘다.
-- ===================================================
ALTER TABLE payments
    MODIFY COLUMN order_id CHAR(36) NOT NULL COMMENT '주문 UUID (cross-domain reference)';

-- ===================================================
-- STEP 2: 중복 결제 방지 제약
-- 한 주문에 결제 시도는 여러 번 있을 수 있지만(실패 후 재시도),
-- "살아 있는" 결제는 동시에 1건만 존재해야 한다.
-- MySQL 에는 부분 인덱스가 없으므로 생성 컬럼 + UNIQUE 로 같은 효과를 낸다.
-- 비활성 상태에서는 NULL 이 되고, UNIQUE 인덱스는 NULL 중복을 허용한다.
-- ===================================================
ALTER TABLE payments
    ADD COLUMN active_order_id CHAR(36)
        GENERATED ALWAYS AS (
            IF(status IN ('PENDING', 'AUTHORIZED', 'CAPTURED', 'PARTIAL_CAPTURED'), order_id, NULL)
            ) STORED COMMENT '활성 상태일 때만 order_id, 아니면 NULL';

ALTER TABLE payments
    ADD CONSTRAINT uk_payments_active_order UNIQUE (active_order_id);

-- ===================================================
-- STEP 3: 조회 인덱스 보강
-- 주문 기준 결제 이력 조회(order_id + 상태 필터)에 사용된다.
-- ===================================================
ALTER TABLE payments
    ADD INDEX idx_payments_order_status (order_id, status);
