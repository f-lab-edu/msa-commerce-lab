package com.msa.commerce.monolith.product.application.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchUseCase;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.monolith.product.domain.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductSearchService implements ProductSearchUseCase {

    private final ProductRepository productRepository;

    private final ProductViewCountPort productViewCountPort;

    private final ProductSearchMapper productSearchMapper;

    @Override
    public ProductPageResponse searchProducts(ProductSearchCommand command) {
        command.validate();

        Page<Product> productPage = productRepository.searchProducts(command);

        Page<ProductSearchResponse> responsePage = productPage.map(this::enrichWithViewCount);

        ProductPageResponse result = productSearchMapper.toPageResponse(responsePage);

        log.info("Found {} products in {} pages", result.getTotalElements(), result.getTotalPages());

        return result;
    }

    private ProductSearchResponse enrichWithViewCount(Product product) {
        ProductSearchResponse response = productSearchMapper.toSearchResponse(product);
        Long viewCount = productViewCountPort.getViewCount(product.getId());

        return ProductSearchResponse.builder()
            .id(response.getId())
            .categoryId(response.getCategoryId())
            .sku(response.getSku())
            .name(response.getName())
            .description(response.getDescription())
            .shortDescription(response.getShortDescription())
            .brand(response.getBrand())
            .model(response.getModel())
            .price(response.getPrice())
            .comparePrice(response.getComparePrice())
            .costPrice(response.getCostPrice())
            .weight(response.getWeight())
            .status(response.getStatus())
            .visibility(response.getVisibility())
            .isFeatured(response.getIsFeatured())
            .createdAt(response.getCreatedAt())
            .updatedAt(response.getUpdatedAt())
            .viewCount(viewCount != null ? viewCount : 0L)
            .build();
    }

}

