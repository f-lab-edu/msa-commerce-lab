-- =====================================================
-- Payment Service Database Schema Migration
-- Version: V3
-- Description: 취소 사유 컬럼 추가 및 Transactional Outbox 테이블 생성
-- =====================================================

USE
db_payment;

-- ===================================================
-- STEP 1: 취소 사유 컬럼
-- V0 에는 failure_reason(실패)과 refund_reason(환불)만 있고
-- 취소 사유를 담을 곳이 없어 둘 중 하나에 억지로 넣어야 했다.
-- ===================================================
ALTER TABLE payments
    ADD COLUMN cancel_reason TEXT NULL COMMENT '결제 취소 사유' AFTER failure_reason;

-- ===================================================
-- STEP 2: Transactional Outbox
-- 결제 상태 변경과 이벤트 적재를 한 트랜잭션으로 묶어 두고,
-- 릴레이가 별도로 Kafka 에 발행한다.
-- DB 커밋은 됐는데 Kafka 발행만 실패해서 주문이 영원히
-- PAYMENT_PENDING 에 멈추는 상황을 막는다.
-- ===================================================
CREATE TABLE IF NOT EXISTS payment_outbox_events
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,

    -- 소비자(order-orchestrator IdempotencyService)의 중복 처리 판단 키
    event_id
    CHAR
(
    36
) NOT NULL UNIQUE,
    event_type VARCHAR
(
    100
) NOT NULL,

    -- Kafka 메시지 키로 사용 (같은 주문의 이벤트 순서 보장)
    aggregate_id CHAR
(
    36
) NOT NULL,
    topic VARCHAR
(
    100
) NOT NULL,
    payload JSON NOT NULL,
    correlation_id CHAR
(
    36
),

    publishing_status ENUM
(
    'PENDING',
    'PUBLISHED',
    'FAILED'
) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    error_message TEXT,

    kafka_partition INT,
    kafka_offset BIGINT,

    occurred_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_outbox_status
(
    publishing_status,
    retry_count
),
    INDEX idx_payment_outbox_aggregate
(
    aggregate_id
),
    INDEX idx_payment_outbox_occurred_at
(
    occurred_at
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;
