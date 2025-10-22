-- =====================================================
-- Order Service Database Schema
-- Version: V1
-- Description: Create orders and order_items tables
-- =====================================================

-- orders 테이블 생성
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '내부 시퀀스 ID',
    order_id VARCHAR(36) NOT NULL UNIQUE COMMENT '주문 UUID',
    order_number VARCHAR(100) NOT NULL UNIQUE COMMENT '주문 번호',
    customer_id BIGINT NOT NULL COMMENT '고객 ID',
    status VARCHAR(20) NOT NULL COMMENT '주문 상태',

    subtotal_amount DECIMAL(12, 4) NOT NULL DEFAULT 0.0000 COMMENT '소계 금액',
    tax_amount DECIMAL(12, 4) NOT NULL DEFAULT 0.0000 COMMENT '세금',
    shipping_amount DECIMAL(12, 4) NOT NULL DEFAULT 0.0000 COMMENT '배송비',
    discount_amount DECIMAL(12, 4) NOT NULL DEFAULT 0.0000 COMMENT '할인 금액',
    total_amount DECIMAL(12, 4) NOT NULL DEFAULT 0.0000 COMMENT '총 금액',
    currency VARCHAR(3) NOT NULL DEFAULT 'KRW' COMMENT '통화',

    shipping_address JSON NOT NULL COMMENT '배송 주소',

    order_date DATETIME(6) NOT NULL COMMENT '주문 일시',
    confirmed_at DATETIME(6) NULL COMMENT '주문 확정 일시',
    payment_completed_at DATETIME(6) NULL COMMENT '결제 완료 일시',
    shipped_at DATETIME(6) NULL COMMENT '배송 시작 일시',
    delivered_at DATETIME(6) NULL COMMENT '배송 완료 일시',
    cancelled_at DATETIME(6) NULL COMMENT '주문 취소 일시',

    source_channel VARCHAR(50) NULL COMMENT '주문 채널',
    version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 락 버전',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 일시',

    INDEX idx_order_id (order_id),
    INDEX idx_order_number (order_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_customer_status (customer_id, status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주문 테이블';

-- order_items 테이블 생성
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '내부 시퀀스 ID',
    order_item_id VARCHAR(36) NOT NULL UNIQUE COMMENT '주문 항목 UUID',
    order_id BIGINT NOT NULL COMMENT '주문 ID (FK)',

    product_id BIGINT NOT NULL COMMENT '상품 ID',
    product_variant_id BIGINT NULL COMMENT '상품 변형 ID',
    product_name VARCHAR(255) NOT NULL COMMENT '상품명',
    product_sku VARCHAR(100) NOT NULL COMMENT '상품 SKU',
    variant_name VARCHAR(255) NULL COMMENT '변형명',

    quantity INT NOT NULL COMMENT '수량',
    unit_price DECIMAL(10, 4) NOT NULL COMMENT '단가',
    total_price DECIMAL(12, 4) NOT NULL COMMENT '총 가격',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,

    INDEX idx_order_item_id (order_item_id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주문 항목 테이블';
