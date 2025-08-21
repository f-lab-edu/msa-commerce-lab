-- V2__Create_products_table.sql
-- 상품 테이블 생성

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 ID',
    name VARCHAR(100) NOT NULL COMMENT '상품명',
    description TEXT COMMENT '상품 설명',
    price DECIMAL(10,2) NOT NULL COMMENT '가격',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '재고 수량',
    category VARCHAR(50) NOT NULL COMMENT '카테고리',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '상품 상태',
    image_url VARCHAR(500) COMMENT '이미지 URL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (id),
    INDEX idx_products_name (name),
    INDEX idx_products_category (category),
    INDEX idx_products_status (status),
    INDEX idx_products_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품 테이블';

-- 카테고리 ENUM 값 확인을 위한 체크 제약조건
ALTER TABLE products ADD CONSTRAINT chk_products_category 
CHECK (category IN ('ELECTRONICS', 'CLOTHING', 'BOOKS', 'HOME_GARDEN', 'SPORTS', 'BEAUTY', 'FOOD', 'TOYS', 'AUTOMOTIVE', 'HEALTH'));

-- 상품 상태 ENUM 값 확인을 위한 체크 제약조건
ALTER TABLE products ADD CONSTRAINT chk_products_status 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK', 'DELETED'));

-- 가격 체크 제약조건
ALTER TABLE products ADD CONSTRAINT chk_products_price 
CHECK (price > 0 AND price <= 10000000);

-- 재고 수량 체크 제약조건
ALTER TABLE products ADD CONSTRAINT chk_products_stock_quantity 
CHECK (stock_quantity >= 0);