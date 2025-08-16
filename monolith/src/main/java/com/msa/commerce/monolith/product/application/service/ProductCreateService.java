package com.msa.commerce.monolith.product.application.service;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 생성 서비스
 * 헥사고날 아키텍처의 애플리케이션 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(ProductCreateCommand command) {
        // 1. 명령 유효성 검증
        command.validate();
        
        // 2. 비즈니스 룰 검증: 중복 상품명 체크
        validateDuplicateProductName(command.getName());
        
        // 3. 도메인 객체 생성
        Product product = Product.builder()
                .name(command.getName())
                .description(command.getDescription())
                .price(command.getPrice())
                .stockQuantity(command.getStockQuantity())
                .category(command.getCategory())
                .imageUrl(command.getImageUrl())
                .build();
        
        // 4. 저장
        Product savedProduct = productRepository.save(product);
        
        // 5. 응답 객체 변환
        return ProductResponse.from(savedProduct);
    }

    /**
     * 중복 상품명 검증
     */
    private void validateDuplicateProductName(String name) {
        if (productRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 상품명입니다: " + name);
        }
    }
}