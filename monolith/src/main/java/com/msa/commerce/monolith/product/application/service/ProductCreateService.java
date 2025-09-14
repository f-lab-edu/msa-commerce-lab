package com.msa.commerce.monolith.product.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.event.ProductEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;

    private final ProductResponseMapper productResponseMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @ValidateCommand(errorPrefix = "Product creation validation failed")
    public ProductResponse createProduct(ProductCreateCommand command) {
        validateCommand(command);
        validateDuplicateSku(command.getSku());
        return executeProductCreation(command);
    }

    private ProductResponse executeProductCreation(ProductCreateCommand command) {
        Product product = Product.builder()
            .sku(command.getSku())
            .name(command.getName())
            .shortDescription(command.getShortDescription())
            .description(command.getDescription())
            .categoryId(command.getCategoryId())
            .brand(command.getBrand())
            .productType(command.getProductType())
            .basePrice(command.getBasePrice())
            .salePrice(command.getSalePrice())
            .currency(command.getCurrency())
            .weightGrams(command.getWeightGrams())
            .requiresShipping(command.getRequiresShipping())
            .isTaxable(command.getIsTaxable())
            .isFeatured(command.getIsFeatured())
            .slug(command.getSlug())
            .searchTags(command.getSearchTags())
            .primaryImageUrl(command.getPrimaryImageUrl())
            .build();

        Product savedProduct = productRepository.save(product);

        // 통합 이벤트 발행 (트랜잭션 커밋 후 캐시 무효화 처리)
        applicationEventPublisher.publishEvent(
            ProductEvent.productCreated(savedProduct)
        );

        return productResponseMapper.toResponse(savedProduct);
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

        if (command.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required.");
        }
    }

}
