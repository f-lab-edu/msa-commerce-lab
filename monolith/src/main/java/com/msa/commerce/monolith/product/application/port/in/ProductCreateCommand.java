package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.domain.ProductCategory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 상품 생성 명령 객체
 * 헥사고날 아키텍처의 인바운드 포트 데이터
 */
@Getter
@Builder
public class ProductCreateCommand {
    
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final ProductCategory category;
    private final String imageUrl;

    /**
     * 자체 유효성 검증
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException("상품명은 100자를 초과할 수 없습니다.");
        }
        
        if (price == null) {
            throw new IllegalArgumentException("가격은 필수입니다.");
        }
        
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        
        if (price.compareTo(new BigDecimal("10000000")) > 0) {
            throw new IllegalArgumentException("가격은 1000만원을 초과할 수 없습니다.");
        }
        
        if (stockQuantity == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }
        
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }
        
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        
        if (imageUrl != null && imageUrl.length() > 500) {
            throw new IllegalArgumentException("이미지 URL은 500자를 초과할 수 없습니다.");
        }
        
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("상품 설명은 2000자를 초과할 수 없습니다.");
        }
    }
}