-- ============================================================================
-- V2: Product 주문 수량 제약조건 및 검색 최적화 인덱스 추가 (#73)
-- ============================================================================

USE
db_platform;

ALTER TABLE products
    ADD CONSTRAINT chk_products_min_order_qty
        CHECK (min_order_quantity IS NULL OR min_order_quantity > 0);

ALTER TABLE products
    ADD CONSTRAINT chk_products_max_order_qty
        CHECK (max_order_quantity IS NULL OR max_order_quantity > 0);

CREATE INDEX idx_products_category_status ON products (category_id, status);

CREATE INDEX idx_products_brand_status ON products (brand, status);
