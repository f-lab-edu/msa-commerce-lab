package com.msa.commerce.monolith.product.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.command.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.event.ProductEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @ValidateCommand(errorPrefix = "Product creation validation failed")
    public ProductResponse createProduct(ProductCreateCommand command) {
        validateCommand(command);
        validateDuplicateSku(command.getSku());
        return executeProductCreation(command);
    }

    private ProductResponse executeProductCreation(ProductCreateCommand command) {
        Product product = productMapper.toProduct(command);
        Product savedProduct = productRepository.save(product);

        // 통합 이벤트 발행 (트랜잭션 커밋 후 캐시 무효화 처리)
        applicationEventPublisher.publishEvent(ProductEvent.productCreated(savedProduct));

        return productMapper.toResponse(savedProduct);
    }

    private void validateDuplicateSku(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException(
                "Product SKU already exists: " + sku,
                ErrorCode.PRODUCT_SKU_DUPLICATE.getCode()
            );
        }
    }

    private void validateCommand(ProductCreateCommand command) {
        // Manual validation based on the command annotations
        if (command.getSku() == null || command.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }

        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (command.getBasePrice() == null) {
            throw new IllegalArgumentException("Base price must be greater than 0.");
        }

        if (command.getBasePrice().signum() <= 0) {
            throw new IllegalArgumentException("Base price must be greater than 0.");
        }

        if (command.getSlug() == null || command.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Slug is required");
        }

        // Optional validation for categoryId - based on business logic, it might be required
        // The test expects exception when categoryId is null, but the annotation doesn't mark it as @NotNull
        // Let's check if this validation is needed based on the failing test

        if (command.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required.");
        }
    }

}
