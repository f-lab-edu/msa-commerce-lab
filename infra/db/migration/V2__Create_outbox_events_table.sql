-- =====================================================
-- Outbox Pattern Implementation
-- Version: V2
-- Description: Create outbox_events table for transactional event publishing
-- =====================================================

USE db_order;

-- ===================================================
-- STEP 1: Create outbox_events table
-- ===================================================

CREATE TABLE IF NOT EXISTS outbox_events (
    id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'UUID',
    aggregate_type VARCHAR(100) NOT NULL COMMENT 'Aggregate type (e.g., Order, Payment)',
    aggregate_id VARCHAR(100) NOT NULL COMMENT 'Aggregate ID (e.g., Order UUID)',
    event_type VARCHAR(100) NOT NULL COMMENT 'Event type (e.g., OrderCreated, OrderUpdated)',
    topic VARCHAR(255) NOT NULL COMMENT 'Kafka topic name',
    payload TEXT NOT NULL COMMENT 'Serialized event payload (JSON)',
    correlation_id VARCHAR(100) COMMENT 'Correlation ID for distributed tracing',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Event status: PENDING, PUBLISHED, FAILED',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Number of retry attempts',
    error_message TEXT COMMENT 'Error message if publishing failed',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Event creation timestamp',
    published_at TIMESTAMP NULL COMMENT 'Event published timestamp',
    last_attempt_at TIMESTAMP NULL COMMENT 'Last publishing attempt timestamp',
    version BIGINT NOT NULL DEFAULT 1 COMMENT 'Optimistic locking version',
    INDEX idx_outbox_status_created (status, created_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Transactional outbox events for reliable event publishing';
