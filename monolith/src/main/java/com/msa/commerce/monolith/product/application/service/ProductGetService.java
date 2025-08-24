package com.msa.commerce.monolith.product.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.common.config.RedisConfig;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductInventory;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductGetService implements ProductGetUseCase {

    private final ProductRepository productRepository;

    private final ProductInventoryRepository inventoryRepository;

    private final ProductViewCountPort viewCountPort;

    private final ProductResponseMapper responseMapper;

    @Override
    public ProductResponse getProduct(Long productId) {
        return getProduct(productId, true);
    }

    @Override
    @Cacheable(value = RedisConfig.PRODUCT_CACHE, key = "#productId",
        condition = "#increaseViewCount == false")
    public ProductResponse getProduct(Long productId, boolean increaseViewCount) {
        // 1. 상품 기본 정보 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format("Product not found with id: %d", productId)));

        // 2. 삭제된 상품 필터링 (ARCHIVED 상태 제외)
        if (ProductStatus.ARCHIVED.equals(product.getStatus())) {
            throw new ResourceNotFoundException(String.format("Product not found with id: %d", productId));
        }

        // 3. 재고 정보 조회
        ProductInventory inventory = inventoryRepository.findByProductId(productId)
            .orElse(null);

        // 4. 응답 객체 생성
        ProductResponse response = responseMapper.toResponse(product);

        // 5. 비동기 조회수 증가 (캐시된 응답이 아닌 경우에만)
        if (increaseViewCount) {
            try {
                viewCountPort.incrementViewCount(productId);
                log.debug("View count increment requested for product ID: {}", productId);
            } catch (Exception e) {
                // 조회수 증가 실패는 전체 프로세스에 영향을 주지 않음
                log.error("Failed to increment view count for product ID: {}", productId, e);
            }
        }

        log.debug("Successfully retrieved product with ID: {}", productId);
        return response;
    }

}
