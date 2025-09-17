-- ============================================================================
-- MSA Commerce Lab - Product Domain Extension
-- Version: V1 (Additional Constraints and Optimizations)
-- Date: 2025-01-21
-- Description: V0에서 누락된 제약조건 및 최적화 추가 (DDL은 V0에 존재)
-- ============================================================================

USE db_platform;

-- 상품 주문 수량 제약 조건 추가 (V0에서 누락된 제약조건)
-- V0에는 chk_products_order_quantity가 있으므로 min/max 개별 제약조건은 필요시에만 추가

-- 제약조건이 존재하지 않는 경우에만 추가
SET @sql = IF((SELECT COUNT(*)
               FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = 'db_platform'
                 AND TABLE_NAME = 'products'
                 AND CONSTRAINT_NAME = 'chk_products_min_order_qty') = 0,
              'ALTER TABLE products ADD CONSTRAINT chk_products_min_order_qty CHECK (min_order_quantity IS NULL OR min_order_quantity > 0)',
              'SELECT "chk_products_min_order_qty constraint already exists" as info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*)
               FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = 'db_platform'
                 AND TABLE_NAME = 'products'
                 AND CONSTRAINT_NAME = 'chk_products_max_order_qty') = 0,
              'ALTER TABLE products ADD CONSTRAINT chk_products_max_order_qty CHECK (max_order_quantity IS NULL OR max_order_quantity > 0)',
              'SELECT "chk_products_max_order_qty constraint already exists" as info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 기본 재고 데이터 설정 (기존 상품들에 대해)
INSERT IGNORE INTO inventory_snapshots (product_id, variant_id, location_code, available_quantity, reserved_quantity, low_stock_threshold)
SELECT
    p.id as product_id,
    NULL as variant_id,
    'MAIN' as location_code,
    FLOOR(RAND() * 100) + 10 as available_quantity,  -- 10~109 랜덤 재고
    0 as reserved_quantity,
    10 as low_stock_threshold
FROM products p
LEFT JOIN inventory_snapshots i ON (p.id = i.product_id AND i.variant_id IS NULL AND i.location_code = 'MAIN')
WHERE i.id IS NULL
AND p.status IN ('ACTIVE', 'INACTIVE');

-- 재고 초기화 이벤트 생성
INSERT INTO inventory_events (
    event_type, aggregate_id, aggregate_version, product_id, variant_id, location_code,
    quantity_change, quantity_before, quantity_after, change_reason,
    reference_type, reference_id, event_data, correlation_id, occurred_at
)
SELECT
    'STOCK_IN' as event_type,
    CONCAT('PRODUCT_', p.id, '_MAIN') as aggregate_id,
    1 as aggregate_version,
    p.id as product_id,
    NULL as variant_id,
    'MAIN' as location_code,
    i.available_quantity as quantity_change,
    0 as quantity_before,
    i.available_quantity as quantity_after,
    '초기 재고 설정' as change_reason,
    'SYSTEM' as reference_type,
    'INITIAL_STOCK' as reference_id,
    JSON_OBJECT('reason', '시스템 초기화', 'admin_id', 'system') as event_data,
    UUID() as correlation_id,
    NOW() as occurred_at
FROM products p
JOIN inventory_snapshots i ON (p.id = i.product_id AND i.variant_id IS NULL AND i.location_code = 'MAIN')
LEFT JOIN inventory_events ie ON (ie.aggregate_id = CONCAT('PRODUCT_', p.id, '_MAIN') AND ie.aggregate_version = 1)
WHERE ie.id IS NULL
AND p.status IN ('ACTIVE', 'INACTIVE');

-- 인덱스 최적화 (V0에서 누락된 인덱스)
-- 이미 존재하지 않는 경우에만 생성
SET @sql = IF((SELECT COUNT(*)
               FROM INFORMATION_SCHEMA.STATISTICS
               WHERE TABLE_SCHEMA = 'db_platform'
                 AND TABLE_NAME = 'products'
                 AND INDEX_NAME = 'idx_products_category_status') = 0,
              'CREATE INDEX idx_products_category_status ON products(category_id, status)',
              'SELECT "idx_products_category_status already exists" as info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*)
               FROM INFORMATION_SCHEMA.STATISTICS
               WHERE TABLE_SCHEMA = 'db_platform'
                 AND TABLE_NAME = 'products'
                 AND INDEX_NAME = 'idx_products_brand_status') = 0,
              'CREATE INDEX idx_products_brand_status ON products(brand, status)',
              'SELECT "idx_products_brand_status already exists" as info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 통계 및 성능을 위한 뷰 생성
CREATE OR REPLACE VIEW v_product_inventory_summary AS
SELECT
    p.id as product_id,
    p.sku,
    p.name as product_name,
    p.status as product_status,
    pc.name as category_name,
    COALESCE(SUM(inv.available_quantity), 0) as total_available,
    COALESCE(SUM(inv.reserved_quantity), 0) as total_reserved,
    COALESCE(SUM(inv.available_quantity + inv.reserved_quantity), 0) as total_quantity,
    CASE
        WHEN COALESCE(SUM(inv.available_quantity), 0) = 0 THEN 'OUT_OF_STOCK'
        WHEN COALESCE(SUM(inv.available_quantity), 0) <= MIN(inv.low_stock_threshold) THEN 'LOW_STOCK'
        ELSE 'IN_STOCK'
    END as stock_status,
    COUNT(pv.id) as variant_count,
    p.created_at,
    p.updated_at
FROM products p
LEFT JOIN product_categories pc ON p.category_id = pc.id
LEFT JOIN inventory_snapshots inv ON p.id = inv.product_id
LEFT JOIN product_variants pv ON p.id = pv.product_id AND pv.status = 'ACTIVE'
WHERE p.status IN ('ACTIVE', 'INACTIVE')
GROUP BY p.id, p.sku, p.name, p.status, pc.name, p.created_at, p.updated_at;

-- 재고 이벤트 요약 뷰
CREATE OR REPLACE VIEW v_inventory_event_summary AS
SELECT
    DATE(ie.occurred_at) as event_date,
    ie.event_type,
    p.name as product_name,
    ie.location_code,
    COUNT(*) as event_count,
    SUM(ie.quantity_change) as total_quantity_change,
    AVG(ie.quantity_change) as avg_quantity_change
FROM inventory_events ie
JOIN products p ON ie.product_id = p.id
WHERE ie.occurred_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(ie.occurred_at), ie.event_type, p.name, ie.location_code
ORDER BY event_date DESC, event_count DESC;

-- 성능 최적화를 위한 프로시저 생성
DELIMITER $$

-- 재고 조정 프로시저
CREATE PROCEDURE sp_adjust_inventory(
    IN p_product_id BIGINT,
    IN p_variant_id BIGINT,
    IN p_location_code VARCHAR(50),
    IN p_quantity_change INT,
    IN p_reason VARCHAR(100),
    IN p_reference_type VARCHAR(50),
    IN p_reference_id VARCHAR(100),
    OUT p_result VARCHAR(100)
)
BEGIN
    DECLARE v_current_available INT DEFAULT 0;
    DECLARE v_current_reserved INT DEFAULT 0;
    DECLARE v_new_available INT DEFAULT 0;
    DECLARE v_aggregate_id VARCHAR(255);
    DECLARE v_next_version BIGINT DEFAULT 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'ERROR: 재고 조정 중 오류 발생';
    END;

    START TRANSACTION;

    -- 현재 재고 조회 및 잠금
    SELECT available_quantity, reserved_quantity
    INTO v_current_available, v_current_reserved
    FROM inventory_snapshots
    WHERE product_id = p_product_id
      AND (p_variant_id IS NULL AND variant_id IS NULL OR variant_id = p_variant_id)
      AND location_code = p_location_code
    FOR UPDATE;

    -- 신규 재고 계산
    SET v_new_available = v_current_available + p_quantity_change;

    -- 재고 유효성 검사
    IF v_new_available < 0 THEN
        SET p_result = 'ERROR: 재고가 부족합니다';
        ROLLBACK;
    ELSE
        -- Aggregate ID 생성
        SET v_aggregate_id = CONCAT('PRODUCT_', p_product_id,
                                   IFNULL(CONCAT('_VARIANT_', p_variant_id), ''),
                                   '_', p_location_code);

        -- 다음 버전 조회
        SELECT COALESCE(MAX(aggregate_version), 0) + 1
        INTO v_next_version
        FROM inventory_events
        WHERE aggregate_id = v_aggregate_id;

        -- 재고 스냅샷 업데이트
        UPDATE inventory_snapshots
        SET available_quantity = v_new_available,
            last_updated_at = NOW(),
            version = version + 1
        WHERE product_id = p_product_id
          AND (p_variant_id IS NULL AND variant_id IS NULL OR variant_id = p_variant_id)
          AND location_code = p_location_code;

        -- 재고 이벤트 생성
        INSERT INTO inventory_events (
            event_type, aggregate_id, aggregate_version, product_id, variant_id,
            location_code, quantity_change, quantity_before, quantity_after,
            change_reason, reference_type, reference_id, event_data,
            correlation_id, occurred_at
        ) VALUES (
            CASE WHEN p_quantity_change > 0 THEN 'STOCK_IN' ELSE 'STOCK_OUT' END,
            v_aggregate_id, v_next_version, p_product_id, p_variant_id,
            p_location_code, p_quantity_change, v_current_available, v_new_available,
            p_reason, p_reference_type, p_reference_id,
            JSON_OBJECT('adjusted_by', 'system', 'timestamp', NOW()),
            UUID(), NOW()
        );

        SET p_result = CONCAT('SUCCESS: 재고가 ', v_current_available, '에서 ', v_new_available, '로 조정되었습니다');
        COMMIT;
    END IF;
END$$

DELIMITER ;

-- 마이그레이션 완료 로그
INSERT INTO cross_domain_events (
    event_uuid, event_type, source_domain, target_domains,
    entity_type, entity_id, entity_uuid, event_data,
    kafka_topic, publishing_status, occurred_at, created_at
) VALUES (
    UUID(), 'MIGRATION_COMPLETED', 'PRODUCT', JSON_ARRAY('INVENTORY', 'ANALYTICS'),
    'MIGRATION', 1, UUID(),
    JSON_OBJECT(
        'migration_version', 'V1',
        'description', 'Product constraints, indexes, views and procedures added',
        'constraints_added', JSON_ARRAY('chk_products_min_order_qty', 'chk_products_max_order_qty'),
        'indexes_added', JSON_ARRAY('idx_products_category_status', 'idx_products_brand_status'),
        'views_created', JSON_ARRAY('v_product_inventory_summary', 'v_inventory_event_summary'),
        'procedures_created', JSON_ARRAY('sp_adjust_inventory')
    ),
    'product.migration.completed', 'PUBLISHED', NOW(), NOW()
);

