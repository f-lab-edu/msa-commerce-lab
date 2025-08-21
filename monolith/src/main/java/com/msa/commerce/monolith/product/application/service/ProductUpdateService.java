package com.msa.commerce.monolith.product.application.service;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.NoChangesProvidedException;
import com.msa.commerce.common.exception.ProductUpdateNotAllowedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductInventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        // 1. 명령 유효성 검증
        command.validate();
        
        // 2. 기존 상품 조회
        Product existingProduct = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + command.getProductId(), 
                        ErrorCode.PRODUCT_NOT_FOUND.getCode()));
        
        // 3. 상품 수정 가능 상태 검증
        if (!existingProduct.isUpdatable()) {
            throw ProductUpdateNotAllowedException.productNotUpdatable(
                    command.getProductId(), existingProduct.getStatus().toString());
        }
        
        // 4. SKU 중복 검증 (SKU가 변경되는 경우)
        if (command.getSkuOptional().isPresent()) {
            String newSku = command.getSkuOptional().get();
            if (!existingProduct.getSku().equals(newSku)) {
                if (productRepository.existsBySku(newSku)) {
                    throw new DuplicateResourceException(
                            "SKU already exists: " + newSku,
                            ErrorCode.PRODUCT_SKU_DUPLICATE.getCode());
                }
            }
        }
        
        // 5. 상품명 중복 검증 (상품명이 변경되는 경우)
        if (command.getNameOptional().isPresent()) {
            String newName = command.getNameOptional().get();
            if (!existingProduct.getName().equals(newName)) {
                if (productRepository.existsByName(newName)) {
                    throw new DuplicateResourceException(
                            "Product name already exists: " + newName,
                            ErrorCode.PRODUCT_NAME_DUPLICATE.getCode());
                }
            }
        }
        
        // 6. 상품 정보 부분 업데이트
        existingProduct.updatePartially(
                command.getCategoryId(),
                command.getSku(),
                command.getName(),
                command.getDescription(),
                command.getShortDescription(),
                command.getBrand(),
                command.getModel(),
                command.getPrice(),
                command.getComparePrice(),
                command.getCostPrice(),
                command.getWeight(),
                command.getProductAttributes(),
                command.getVisibility(),
                command.getTaxClass(),
                command.getMetaTitle(),
                command.getMetaDescription(),
                command.getSearchKeywords(),
                command.getIsFeatured()
        );
        
        // 7. 상품 저장
        Product updatedProduct = productRepository.save(existingProduct);
        
        // 8. 재고 정보 업데이트 (재고 관련 필드가 제공된 경우)
        if (hasInventoryUpdates(command)) {
            updateProductInventory(updatedProduct.getId(), command);
        }
        
        // 9. 캐시 무효화 처리
        invalidateProductCache(updatedProduct.getId());
        
        // 10. 변경 이력 로깅 (추후 변경 이력 테이블 구현 시 활용)
        logProductChanges(existingProduct, updatedProduct, command);
        
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        
        return productResponseMapper.toResponse(updatedProduct);
    }
    
    private boolean hasInventoryUpdates(ProductUpdateCommand command) {
        return command.getInitialStockOptional().isPresent() ||
                command.getLowStockThresholdOptional().isPresent() ||
                command.getIsTrackingEnabledOptional().isPresent() ||
                command.getIsBackorderAllowedOptional().isPresent() ||
                command.getMinOrderQuantityOptional().isPresent() ||
                command.getMaxOrderQuantityOptional().isPresent() ||
                command.getReorderPointOptional().isPresent() ||
                command.getReorderQuantityOptional().isPresent() ||
                command.getLocationCodeOptional().isPresent();
    }
    
    private void updateProductInventory(Long productId, ProductUpdateCommand command) {
        log.debug("Updating inventory for product ID: {}", productId);
        
        // 기존 재고 정보 조회 또는 새로 생성
        ProductInventory inventory = productInventoryRepository.findByProductId(productId)
                .orElse(null);
        
        if (inventory != null) {
            // 기존 재고 정보 업데이트
            inventory.updatePartially(
                    command.getInitialStock(),
                    command.getLowStockThreshold(),
                    command.getIsTrackingEnabled(),
                    command.getIsBackorderAllowed(),
                    command.getMinOrderQuantity(),
                    command.getMaxOrderQuantity(),
                    command.getReorderPoint(),
                    command.getReorderQuantity(),
                    command.getLocationCode()
            );
            productInventoryRepository.save(inventory);
        } else if (command.getInitialStockOptional().isPresent()) {
            // 새로운 재고 정보 생성 (초기 재고가 제공된 경우만)
            ProductInventory newInventory = ProductInventory.builder()
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
            productInventoryRepository.save(newInventory);
        }
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
        
        if (command.getCategoryId() != null) description.append("categoryId,");
        if (command.getSku() != null) description.append("sku,");
        if (command.getName() != null) description.append("name,");
        if (command.getDescription() != null) description.append("description,");
        if (command.getShortDescription() != null) description.append("shortDescription,");
        if (command.getBrand() != null) description.append("brand,");
        if (command.getModel() != null) description.append("model,");
        if (command.getPrice() != null) description.append("price,");
        if (command.getComparePrice() != null) description.append("comparePrice,");
        if (command.getCostPrice() != null) description.append("costPrice,");
        if (command.getWeight() != null) description.append("weight,");
        if (command.getProductAttributes() != null) description.append("productAttributes,");
        if (command.getVisibility() != null) description.append("visibility,");
        if (command.getTaxClass() != null) description.append("taxClass,");
        if (command.getMetaTitle() != null) description.append("metaTitle,");
        if (command.getMetaDescription() != null) description.append("metaDescription,");
        if (command.getSearchKeywords() != null) description.append("searchKeywords,");
        if (command.getIsFeatured() != null) description.append("isFeatured,");
        
        // 재고 관련 필드
        if (hasInventoryUpdates(command)) description.append("inventory,");
        
        return description.length() > 0 ? description.substring(0, description.length() - 1) : "none";
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