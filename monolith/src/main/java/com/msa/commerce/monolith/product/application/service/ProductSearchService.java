package com.msa.commerce.monolith.product.application.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchUseCase;
import com.msa.commerce.monolith.product.application.port.in.command.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
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

    private final ProductMapper productMapper;

    @Override
    @ValidateCommand(errorPrefix = "Product search validation failed")
    public ProductPageResponse searchProducts(ProductSearchCommand command) {
        // Manual validation until AOP is properly configured
        validateCommand(command);

        Page<Product> productPage = productRepository.searchProducts(command);

        if (productPage == null) {
            throw new ResourceNotFoundException("Repository returned null result for search command");
        }

        Page<ProductSearchResponse> responsePage = productPage.map(this::enrichWithViewCount);

        return productMapper.toPageResponse(responsePage);
    }

    private ProductSearchResponse enrichWithViewCount(Product product) {
        Long viewCount = productViewCountPort.getViewCount(product.getId());
        return productMapper.toSearchResponseWithViewCount(product, viewCount != null ? viewCount : 0L);
    }

    private void validateCommand(ProductSearchCommand command) {
        // Manual validation based on the command annotations
        if (command.getPage() != null && command.getPage() < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }

        if (command.getSize() != null && (command.getSize() < 1 || command.getSize() > 100)) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        if (command.getMinPrice() != null && command.getMinPrice().signum() < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }

        if (command.getMaxPrice() != null && command.getMaxPrice().signum() < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }

        // Custom business rule validation
        if (command.getMinPrice() != null && command.getMaxPrice() != null) {
            if (command.getMinPrice().compareTo(command.getMaxPrice()) > 0) {
                throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
            }
        }
    }

}
