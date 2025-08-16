package com.msa.commerce.monolith.product.application.port.in;

/**
 * 상품 생성 Use Case 인터페이스
 * 헥사고날 아키텍처의 인바운드 포트
 */
public interface ProductCreateUseCase {
    
    /**
     * 상품 생성
     * 
     * @param command 상품 생성 명령
     * @return 생성된 상품 정보
     */
    ProductResponse createProduct(ProductCreateCommand command);
}