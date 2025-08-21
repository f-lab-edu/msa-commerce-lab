package com.msa.commerce.monolith.product.application.port.in;

public interface ProductUpdateUseCase {
    
    /**
     * 상품 정보를 업데이트합니다.
     * 
     * @param command 업데이트할 상품 정보
     * @return 업데이트된 상품 정보
     * @throws IllegalArgumentException 유효하지 않은 입력값인 경우
     * @throws RuntimeException 상품을 찾을 수 없거나 업데이트할 수 없는 상태인 경우
     */
    ProductResponse updateProduct(ProductUpdateCommand command);
}