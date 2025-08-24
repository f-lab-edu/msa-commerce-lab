package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ProductUpdateNotAllowedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductInventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductUpdateService implements ProductUpdateUseCase {

    private final ProductRepository productRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final ProductResponseMapper productResponseMapper;

    @Override
    public ProductResponse updateProduct(ProductUpdateCommand command) {
        log.info("Updating product with ID: {}", command.getProductId());

        command.validate();
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
        command.getSkuOptional().ifPresent(newSku -> {
            if (!existingProduct.getSku().equals(newSku) && productRepository.existsBySku(newSku)) {
                throw new DuplicateResourceException(
                    "SKU already exists: " + newSku,
                    ErrorCode.PRODUCT_SKU_DUPLICATE.getCode());
            }
        });
    }

    private void validateNameUnique(ProductUpdateCommand command, Product existingProduct) {
        command.getNameOptional().ifPresent(newName -> {
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

    private void performPostUpdateOperations(Product updatedProduct, ProductUpdateCommand command, Product originalProduct) {
        updateInventoryIfNeeded(updatedProduct.getId(), command);
        invalidateProductCache(updatedProduct.getId());
        logProductChanges(originalProduct, updatedProduct, command);
    }

    private void updateInventoryIfNeeded(Long productId, ProductUpdateCommand command) {
        if (hasInventoryUpdates(command)) {
            updateProductInventory(productId, command);
        }
    }

    private boolean hasInventoryUpdates(ProductUpdateCommand command) {
        return hasStockUpdates(command) || hasInventoryConfigUpdates(command) || hasOrderQuantityUpdates(command);
    }

    private boolean hasStockUpdates(ProductUpdateCommand command) {
        return command.getInitialStockOptional().isPresent() ||
            command.getLowStockThresholdOptional().isPresent();
    }

    private boolean hasInventoryConfigUpdates(ProductUpdateCommand command) {
        return command.getIsTrackingEnabledOptional().isPresent() ||
            command.getIsBackorderAllowedOptional().isPresent() ||
            command.getLocationCodeOptional().isPresent();
    }

    private boolean hasOrderQuantityUpdates(ProductUpdateCommand command) {
        return command.getMinOrderQuantityOptional().isPresent() ||
            command.getMaxOrderQuantityOptional().isPresent() ||
            command.getReorderPointOptional().isPresent() ||
            command.getReorderQuantityOptional().isPresent();
    }

    private void updateProductInventory(Long productId, ProductUpdateCommand command) {
        log.debug("Updating inventory for product ID: {}", productId);

        ProductInventory inventory = productInventoryRepository.findByProductId(productId).orElse(null);
        
        if (inventory != null) {
            updateExistingInventory(inventory, command);
        } else {
            createNewInventoryIfNeeded(productId, command);
        }
    }

    private void updateExistingInventory(ProductInventory inventory, ProductUpdateCommand command) {
        inventory.updatePartially(
            command.getInitialStock(), command.getLowStockThreshold(),
            command.getIsTrackingEnabled(), command.getIsBackorderAllowed(),
            command.getMinOrderQuantity(), command.getMaxOrderQuantity(),
            command.getReorderPoint(), command.getReorderQuantity(),
            command.getLocationCode()
        );
        productInventoryRepository.save(inventory);
    }

    private void createNewInventoryIfNeeded(Long productId, ProductUpdateCommand command) {
        if (command.getInitialStockOptional().isPresent()) {
            ProductInventory newInventory = buildNewInventory(productId, command);
            productInventoryRepository.save(newInventory);
        }
    }

    private ProductInventory buildNewInventory(Long productId, ProductUpdateCommand command) {
        return ProductInventory.builder()
            .productId(productId)
            .availableQuantity(command.getInitialStock())
            .reservedQuantity(0)
            .totalQuantity(command.getInitialStock())
            .lowStockThreshold(command.getLowStockThreshold())
            .isTrackingEnabled(command.getIsTrackingEnabled())
            .isBackorderAllowed(command.getIsBackorderAllowed())
            .minOrderQuantity(command.getMinOrderQuantity())
            .maxOrderQuantity(command.getMaxOrderQuantity())
            .reorderPoint(command.getReorderPoint())
            .reorderQuantity(command.getReorderQuantity())
            .locationCode(command.getLocationCode())
            .build();
    }

    private void logProductChanges(Product before, Product after, ProductUpdateCommand command) {
        // 변경 이력 로깅
        log.info("Product changes for ID: {} - Updated fields: {}",
            after.getId(), getUpdatedFieldsDescription(command));

        // 추후 변경 이력 테이블이 구현되면 여기서 변경 이력을 저장
        // productHistoryRepository.save(ProductHistory.create(before, after, command));
    }

    private String getUpdatedFieldsDescription(ProductUpdateCommand command) {
        StringBuilder description = new StringBuilder();
        
        appendBasicFieldUpdates(description, command);
        appendPriceFieldUpdates(description, command);
        appendMetadataFieldUpdates(description, command);
        appendInventoryFieldUpdates(description, command);
        
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

    private void appendInventoryFieldUpdates(StringBuilder description, ProductUpdateCommand command) {
        if (hasInventoryUpdates(command)) {
            description.append("inventory,");
        }
    }

    private void appendFieldIfNotNull(StringBuilder description, Object field, String fieldName) {
        if (field != null) {
            description.append(fieldName).append(",");
        }
    }

    private void invalidateProductCache(Long productId) {
        // 캐시 무효화 처리
        log.debug("Invalidating cache for product ID: {}", productId);

        // 추후 캐시 시스템이 구축되면 다음과 같은 캐시 무효화 로직 구현:
        // 1. 단일 상품 캐시 무효화: @CacheEvict(value = "products", key = "#productId")
        // 2. 카테고리별 상품 목록 캐시 무효화
        // 3. 추천 상품 목록 캐시 무효화
        // 4. 검색 결과 캐시 무효화

        // 현재는 로깅만 수행
        log.info("Cache invalidation completed for product ID: {}", productId);

        // 추후 구현 예시:
        // cacheManager.evict("products", productId);
        // cacheManager.evict("products-by-category", product.getCategoryId());
        // cacheManager.evict("featured-products");
        // searchCacheService.invalidateSearchCache();
    }

}
