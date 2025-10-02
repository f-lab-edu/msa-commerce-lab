package com.msa.commerce.monolith.product.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCacheService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    @Cacheable(value = "product", key = "#productId")
    public ProductResponse getCachedProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format("Product not found with id: %d", productId)));

        if (ProductStatus.ARCHIVED.equals(product.getStatus())) {
            throw new ResourceNotFoundException(String.format("Product not found with id: %d", productId));
        }

        return productMapper.toResponse(product);
    }

}
