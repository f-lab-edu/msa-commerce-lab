package com.msa.commerce.monolith.product.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.config.RedisConfig;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductGetService implements ProductGetUseCase {

    private final ProductRepository productRepository;

    private final ProductViewCountPort viewCountPort;

    private final ProductResponseMapper responseMapper;

    @Override
    public ProductResponse getProduct(Long productId) {
        return getProduct(productId, true);
    }

    @Override
    @Cacheable(value = RedisConfig.PRODUCT_CACHE, key = "#productId", condition = "#increaseViewCount == false")
    public ProductResponse getProduct(Long productId, boolean increaseViewCount) {
        Product product = productRepository.findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format("Product not found with id: %d", productId)));

        if (ProductStatus.ARCHIVED.equals(product.getStatus())) {
            throw new ResourceNotFoundException(String.format("Product not found with id: %d", productId));
        }

        ProductResponse response = responseMapper.toResponse(product);

        if (increaseViewCount) {
            try {
                viewCountPort.incrementViewCount(productId);
            } catch (Exception e) {
                log.error("Failed to increment view count for product ID: {}", productId, e);
            }
        }
        return response;
    }

    @Override
    public ProductPageResponse searchProducts(ProductSearchCommand searchCommand) {
        var productPage = productRepository.searchProducts(searchCommand);
        return ProductPageResponse.from(productPage, responseMapper::toSearchResponse);
    }

}
