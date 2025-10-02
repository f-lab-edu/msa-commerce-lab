package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.command.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductGetService implements ProductGetUseCase {

    private final ProductCacheService productCacheService;

    private final ProductRepository productRepository;

    private final ProductViewCountPort viewCountPort;

    private final ProductMapper productMapper;

    @Override
    public ProductResponse getProduct(Long productId) {
        ProductResponse response = productCacheService.getCachedProduct(productId);

        try {
            viewCountPort.incrementViewCount(productId);
        } catch (Exception e) {
            log.error("Failed to increment view count for product ID: {}", productId, e);
        }

        return response;
    }

    @Override
    public ProductResponse getProduct(Long productId, boolean increaseViewCount) {
        if (increaseViewCount) {
            return getProduct(productId);
        }
        // 조회수 증가 없이 캐시된 데이터 반환
        return productCacheService.getCachedProduct(productId);
    }

    @Override
    public ProductPageResponse searchProducts(ProductSearchCommand searchCommand) {
        var productPage = productRepository.searchProducts(searchCommand);
        var responsePage = productPage.map(productMapper::toSearchResponse);
        return productMapper.toPageResponse(responsePage);
    }

}
