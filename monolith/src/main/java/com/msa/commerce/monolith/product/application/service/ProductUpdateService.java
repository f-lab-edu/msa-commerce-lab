package com.msa.commerce.monolith.product.application.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ProductUpdateNotAllowedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.adapter.out.persistence.ProductJpaEntity;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.in.command.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
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

    private final ProductMapper productMapper;

    private final Validator validator;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @ValidateCommand(errorPrefix = "Product update validation failed")
    public ProductResponse updateProduct(ProductUpdateCommand command) {
        validateCommand(command);

        ProductJpaEntity entity = productRepository.findEntityById(command.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with ID: " + command.getProductId(),
                ErrorCode.PRODUCT_NOT_FOUND.getCode()));

        validateProductUpdatable(entity, command.getProductId());
        validateUniqueConstraints(command, entity);

        entity.updateProductInfo(
            command.getSku(), command.getName(), command.getShortDescription(),
            command.getDescription(), command.getCategoryId(), command.getBrand(),
            command.getProductType(), command.getBasePrice(), command.getSalePrice(),
            command.getCurrency(), command.getWeightGrams(), command.getRequiresShipping(),
            command.getIsTaxable(), command.getIsFeatured(), command.getSlug(),
            command.getSearchTags(), command.getPrimaryImageUrl(),
            command.getMinOrderQuantity(), command.getMaxOrderQuantity()
        );

        applicationEventPublisher.publishEvent(ProductEvent.productUpdated(productMapper.entityToDomain(entity)));

        return productMapper.entityToResponse(entity);
    }

    private void validateProductUpdatable(ProductJpaEntity entity, Long productId) {
        if (!entity.isUpdatable()) {
            throw ProductUpdateNotAllowedException.productNotUpdatable(
                productId, entity.getStatus().toString());
        }
    }

    private void validateUniqueConstraints(ProductUpdateCommand command, ProductJpaEntity entity) {
        validateSkuUnique(command, entity);
        validateNameUnique(command, entity);
    }

    private void validateSkuUnique(ProductUpdateCommand command, ProductJpaEntity entity) {
        Optional.ofNullable(command.getSku()).ifPresent(newSku -> {
            if (!entity.getSku().equals(newSku) && productRepository.existsBySku(newSku)) {
                throw new DuplicateResourceException(
                    "SKU already exists: " + newSku,
                    ErrorCode.PRODUCT_SKU_DUPLICATE.getCode());
            }
        });
    }

    private void validateNameUnique(ProductUpdateCommand command, ProductJpaEntity entity) {
        Optional.ofNullable(command.getName()).ifPresent(newName -> {
            if (!entity.getName().equals(newName) && productRepository.existsByName(newName)) {
                throw new DuplicateResourceException(
                    "Product name already exists: " + newName,
                    ErrorCode.PRODUCT_NAME_DUPLICATE.getCode());
            }
        });
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
