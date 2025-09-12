package com.msa.commerce.monolith.product.application.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ProductUpdateNotAllowedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.event.ProductEvent;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductUpdateService implements ProductUpdateUseCase {

    private final ProductRepository productRepository;

    private final ProductResponseMapper productResponseMapper;

    private final Validator validator;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @ValidateCommand(errorPrefix = "Product update validation failed")
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#command.productId"),
        @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse updateProduct(ProductUpdateCommand command) {
        validateCommand(command);
        Product existingProduct = findAndValidateProduct(command.getProductId());
        validateProductUpdatable(existingProduct, command.getProductId());
        validateUniqueConstraints(command, existingProduct);

        updateProductData(existingProduct, command);
        Product updatedProduct = productRepository.save(existingProduct);

        performPostUpdateOperations(updatedProduct, command, existingProduct);

        // 통합 이벤트 발행 (트랜잭션 커밋 후 처리됨)
        // 캐시 무효화를 한 번에 처리
        applicationEventPublisher.publishEvent(
            ProductEvent.productUpdated(updatedProduct)
        );

        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return productResponseMapper.toResponse(updatedProduct);
    }

    private Product findAndValidateProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with ID: " + productId,
                ErrorCode.PRODUCT_NOT_FOUND.getCode()));
    }

    private void validateProductUpdatable(Product product, Long productId) {
        if (!product.isUpdatable()) {
            throw ProductUpdateNotAllowedException.productNotUpdatable(
                productId, product.getStatus().toString());
        }
    }

    private void validateUniqueConstraints(ProductUpdateCommand command, Product existingProduct) {
        validateSkuUnique(command, existingProduct);
        validateNameUnique(command, existingProduct);
    }

    private void validateSkuUnique(ProductUpdateCommand command, Product existingProduct) {
        Optional.ofNullable(command.getSku()).ifPresent(newSku -> {
            if (!existingProduct.getSku().equals(newSku) && productRepository.existsBySku(newSku)) {
                throw new DuplicateResourceException(
                    "SKU already exists: " + newSku,
                    ErrorCode.PRODUCT_SKU_DUPLICATE.getCode());
            }
        });
    }

    private void validateNameUnique(ProductUpdateCommand command, Product existingProduct) {
        Optional.ofNullable(command.getName()).ifPresent(newName -> {
            if (!existingProduct.getName().equals(newName) && productRepository.existsByName(newName)) {
                throw new DuplicateResourceException(
                    "Product name already exists: " + newName,
                    ErrorCode.PRODUCT_NAME_DUPLICATE.getCode());
            }
        });
    }

    private void updateProductData(Product product, ProductUpdateCommand command) {
        product.updatePartially(
            command.getSku(), command.getName(), command.getShortDescription(),
            command.getDescription(), command.getCategoryId(), command.getBrand(),
            command.getProductType(), command.getBasePrice(), command.getSalePrice(),
            command.getCurrency(), command.getWeightGrams(), command.getRequiresShipping(),
            command.getIsTaxable(), command.getIsFeatured(), command.getSlug(),
            command.getSearchTags(), command.getPrimaryImageUrl()
        );
    }

    private void performPostUpdateOperations(Product updatedProduct, ProductUpdateCommand command,
        Product originalProduct) {
        logProductChanges(originalProduct, updatedProduct, command);
    }

    private void logProductChanges(Product before, Product after, ProductUpdateCommand command) {
        log.info("Product changes for ID: {} - Updated fields: {}",
            after.getId(), getUpdatedFieldsDescription(command));
    }

    private String getUpdatedFieldsDescription(ProductUpdateCommand command) {
        StringBuilder description = new StringBuilder();

        appendBasicFieldUpdates(description, command);
        appendPriceFieldUpdates(description, command);
        appendMetadataFieldUpdates(description, command);

        return description.length() > 0 ? description.substring(0, description.length() - 1) : "none";
    }

    private void appendBasicFieldUpdates(StringBuilder description, ProductUpdateCommand command) {
        appendFieldIfNotNull(description, command.getSku(), "sku");
        appendFieldIfNotNull(description, command.getName(), "name");
        appendFieldIfNotNull(description, command.getShortDescription(), "shortDescription");
        appendFieldIfNotNull(description, command.getDescription(), "description");
        appendFieldIfNotNull(description, command.getCategoryId(), "categoryId");
        appendFieldIfNotNull(description, command.getBrand(), "brand");
        appendFieldIfNotNull(description, command.getProductType(), "productType");
    }

    private void appendPriceFieldUpdates(StringBuilder description, ProductUpdateCommand command) {
        appendFieldIfNotNull(description, command.getBasePrice(), "basePrice");
        appendFieldIfNotNull(description, command.getSalePrice(), "salePrice");
        appendFieldIfNotNull(description, command.getCurrency(), "currency");
        appendFieldIfNotNull(description, command.getWeightGrams(), "weightGrams");
    }

    private void appendMetadataFieldUpdates(StringBuilder description, ProductUpdateCommand command) {
        appendFieldIfNotNull(description, command.getRequiresShipping(), "requiresShipping");
        appendFieldIfNotNull(description, command.getIsTaxable(), "isTaxable");
        appendFieldIfNotNull(description, command.getIsFeatured(), "isFeatured");
        appendFieldIfNotNull(description, command.getSlug(), "slug");
        appendFieldIfNotNull(description, command.getSearchTags(), "searchTags");
        appendFieldIfNotNull(description, command.getPrimaryImageUrl(), "primaryImageUrl");
    }

    private void appendFieldIfNotNull(StringBuilder description, Object field, String fieldName) {
        if (field != null) {
            description.append(fieldName).append(",");
        }
    }

    private void validateCommand(ProductUpdateCommand command) {
        Set<ConstraintViolation<ProductUpdateCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Product update validation failed: ");
            violations.forEach(violation -> {
                if (sb.length() > "Product update validation failed: ".length()) {
                    sb.append(", ");
                }
                sb.append(violation.getMessage());
            });
            throw new IllegalArgumentException(sb.toString());
        }
    }

}
