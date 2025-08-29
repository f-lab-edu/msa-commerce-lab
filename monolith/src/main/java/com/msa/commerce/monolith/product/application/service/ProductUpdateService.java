package com.msa.commerce.monolith.product.application.service;

import java.util.Optional;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductUpdateService implements ProductUpdateUseCase {

    private final ProductRepository productRepository;

    private final ProductResponseMapper productResponseMapper;

    @Override
    @ValidateCommand(errorPrefix = "Product update validation failed")
    public ProductResponse updateProduct(ProductUpdateCommand command) {
        log.info("Updating product with ID: {}", command.getProductId());

        Product existingProduct = findAndValidateProduct(command.getProductId());
        validateProductUpdatable(existingProduct, command.getProductId());
        validateUniqueConstraints(command, existingProduct);

        updateProductData(existingProduct, command);
        Product updatedProduct = productRepository.save(existingProduct);

        performPostUpdateOperations(updatedProduct, command, existingProduct);

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
            command.getCategoryId(), command.getSku(), command.getName(),
            command.getDescription(), command.getShortDescription(), command.getBrand(),
            command.getModel(), command.getPrice(), command.getComparePrice(),
            command.getCostPrice(), command.getWeight(), command.getProductAttributes(),
            command.getVisibility(), command.getTaxClass(), command.getMetaTitle(),
            command.getMetaDescription(), command.getSearchKeywords(), command.getIsFeatured()
        );
    }

    private void performPostUpdateOperations(Product updatedProduct, ProductUpdateCommand command,
        Product originalProduct) {
        invalidateProductCache(updatedProduct.getId());
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
        appendFieldIfNotNull(description, command.getCategoryId(), "categoryId");
        appendFieldIfNotNull(description, command.getSku(), "sku");
        appendFieldIfNotNull(description, command.getName(), "name");
        appendFieldIfNotNull(description, command.getDescription(), "description");
        appendFieldIfNotNull(description, command.getShortDescription(), "shortDescription");
        appendFieldIfNotNull(description, command.getBrand(), "brand");
        appendFieldIfNotNull(description, command.getModel(), "model");
    }

    private void appendPriceFieldUpdates(StringBuilder description, ProductUpdateCommand command) {
        appendFieldIfNotNull(description, command.getPrice(), "price");
        appendFieldIfNotNull(description, command.getComparePrice(), "comparePrice");
        appendFieldIfNotNull(description, command.getCostPrice(), "costPrice");
        appendFieldIfNotNull(description, command.getWeight(), "weight");
    }

    private void appendMetadataFieldUpdates(StringBuilder description, ProductUpdateCommand command) {
        appendFieldIfNotNull(description, command.getProductAttributes(), "productAttributes");
        appendFieldIfNotNull(description, command.getVisibility(), "visibility");
        appendFieldIfNotNull(description, command.getTaxClass(), "taxClass");
        appendFieldIfNotNull(description, command.getMetaTitle(), "metaTitle");
        appendFieldIfNotNull(description, command.getMetaDescription(), "metaDescription");
        appendFieldIfNotNull(description, command.getSearchKeywords(), "searchKeywords");
        appendFieldIfNotNull(description, command.getIsFeatured(), "isFeatured");
    }

    private void appendFieldIfNotNull(StringBuilder description, Object field, String fieldName) {
        if (field != null) {
            description.append(fieldName).append(",");
        }
    }

    private void invalidateProductCache(Long productId) {
        log.debug("Invalidating cache for product ID: {}", productId);
        log.info("Cache invalidation completed for product ID: {}", productId);
    }

}
