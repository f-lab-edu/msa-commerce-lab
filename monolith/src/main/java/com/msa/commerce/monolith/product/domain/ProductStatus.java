package com.msa.commerce.monolith.product.domain;

/**
 * 상품 상태 열거형
 */
public enum ProductStatus {
    
    /**
     * 활성 상태 - 판매 가능
     */
    ACTIVE,
    
    /**
     * 비활성 상태 - 판매 불가
     */
    INACTIVE,
    
    /**
     * 품절 상태
     */
    OUT_OF_STOCK,
    
    /**
     * 삭제된 상태
     */
    DELETED
}