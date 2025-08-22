package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductUpdateCommand {

    private final Long productId;                   // 업데이트할 상품 ID

    private final Long categoryId;

    private final String sku;

    private final String name;

    private final String description;

    private final String shortDescription;

    private final String brand;

    private final String model;

    private final BigDecimal price;

    private final BigDecimal comparePrice;

    private final BigDecimal costPrice;

    private final BigDecimal weight;

    private final String productAttributes;

    private final String visibility;

    private final String taxClass;

    private final String metaTitle;

    private final String metaDescription;

    private final String searchKeywords;

    private final Boolean isFeatured;

    // 재고 관련 (별도 ProductInventory로 관리)
    private final Integer initialStock;

    private final Integer lowStockThreshold;

    private final Boolean isTrackingEnabled;

    private final Boolean isBackorderAllowed;

    // 확장된 재고 관리 필드
    private final Integer minOrderQuantity;

    private final Integer maxOrderQuantity;

    private final Integer reorderPoint;

    private final Integer reorderQuantity;

    private final String locationCode;

    // Optional 래퍼 메소드들 - 부분 업데이트 지원
    public Optional<Long> getCategoryIdOptional() {
        return Optional.ofNullable(categoryId);
    }

    public Optional<String> getSkuOptional() {
        return Optional.ofNullable(sku);
    }

    public Optional<String> getNameOptional() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getDescriptionOptional() {
        return Optional.ofNullable(description);
    }

    public Optional<String> getShortDescriptionOptional() {
        return Optional.ofNullable(shortDescription);
    }

    public Optional<String> getBrandOptional() {
        return Optional.ofNullable(brand);
    }

    public Optional<String> getModelOptional() {
        return Optional.ofNullable(model);
    }

    public Optional<BigDecimal> getPriceOptional() {
        return Optional.ofNullable(price);
    }

    public Optional<BigDecimal> getComparePriceOptional() {
        return Optional.ofNullable(comparePrice);
    }

    public Optional<BigDecimal> getCostPriceOptional() {
        return Optional.ofNullable(costPrice);
    }

    public Optional<BigDecimal> getWeightOptional() {
        return Optional.ofNullable(weight);
    }

    public Optional<String> getProductAttributesOptional() {
        return Optional.ofNullable(productAttributes);
    }

    public Optional<String> getVisibilityOptional() {
        return Optional.ofNullable(visibility);
    }

    public Optional<String> getTaxClassOptional() {
        return Optional.ofNullable(taxClass);
    }

    public Optional<String> getMetaTitleOptional() {
        return Optional.ofNullable(metaTitle);
    }

    public Optional<String> getMetaDescriptionOptional() {
        return Optional.ofNullable(metaDescription);
    }

    public Optional<String> getSearchKeywordsOptional() {
        return Optional.ofNullable(searchKeywords);
    }

    public Optional<Boolean> getIsFeaturedOptional() {
        return Optional.ofNullable(isFeatured);
    }

    public Optional<Integer> getInitialStockOptional() {
        return Optional.ofNullable(initialStock);
    }

    public Optional<Integer> getLowStockThresholdOptional() {
        return Optional.ofNullable(lowStockThreshold);
    }

    public Optional<Boolean> getIsTrackingEnabledOptional() {
        return Optional.ofNullable(isTrackingEnabled);
    }

    public Optional<Boolean> getIsBackorderAllowedOptional() {
        return Optional.ofNullable(isBackorderAllowed);
    }

    public Optional<Integer> getMinOrderQuantityOptional() {
        return Optional.ofNullable(minOrderQuantity);
    }

    public Optional<Integer> getMaxOrderQuantityOptional() {
        return Optional.ofNullable(maxOrderQuantity);
    }

    public Optional<Integer> getReorderPointOptional() {
        return Optional.ofNullable(reorderPoint);
    }

    public Optional<Integer> getReorderQuantityOptional() {
        return Optional.ofNullable(reorderQuantity);
    }

    public Optional<String> getLocationCodeOptional() {
        return Optional.ofNullable(locationCode);
    }

    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required for update.");
        }

        // SKU 유효성 검증 (명시된 경우만)
        if (sku != null && (sku.trim().isEmpty() || sku.length() > 100)) {
            throw new IllegalArgumentException("SKU must not be empty and cannot exceed 100 characters.");
        }

        // 상품명 유효성 검증 (명시된 경우만)
        if (name != null && (name.trim().isEmpty() || name.length() > 255)) {
            throw new IllegalArgumentException("Product name must not be empty and cannot exceed 255 characters.");
        }

        // 가격 유효성 검증 (명시된 경우만)
        if (price != null && (price.compareTo(BigDecimal.ZERO) <= 0
            || price.compareTo(new BigDecimal("99999999.99")) > 0)) {
            throw new IllegalArgumentException("Price must be between 0.01 and 99,999,999.99.");
        }

        // 설명 필드 길이 검증
        if (description != null && description.length() > 5000) {
            throw new IllegalArgumentException("Product description cannot exceed 5000 characters.");
        }

        if (shortDescription != null && shortDescription.length() > 500) {
            throw new IllegalArgumentException("Short description cannot exceed 500 characters.");
        }

        if (brand != null && brand.length() > 100) {
            throw new IllegalArgumentException("Brand cannot exceed 100 characters.");
        }

        if (model != null && model.length() > 100) {
            throw new IllegalArgumentException("Model cannot exceed 100 characters.");
        }

        // 재고 관련 유효성 검증
        if (initialStock != null && initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }

        if (lowStockThreshold != null && lowStockThreshold < 0) {
            throw new IllegalArgumentException("Low stock threshold cannot be negative.");
        }

        if (minOrderQuantity != null && minOrderQuantity <= 0) {
            throw new IllegalArgumentException("Minimum order quantity must be positive.");
        }

        if (maxOrderQuantity != null && minOrderQuantity != null && maxOrderQuantity < minOrderQuantity) {
            throw new IllegalArgumentException("Maximum order quantity cannot be less than minimum order quantity.");
        }

        if (reorderPoint != null && reorderPoint < 0) {
            throw new IllegalArgumentException("Reorder point cannot be negative.");
        }

        if (reorderQuantity != null && reorderQuantity < 0) {
            throw new IllegalArgumentException("Reorder quantity cannot be negative.");
        }

        if (locationCode != null && locationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Location code cannot be empty.");
        }
    }

    // 변경 사항이 있는지 확인
    public boolean hasChanges() {
        return categoryId != null || sku != null || name != null || description != null ||
            shortDescription != null || brand != null || model != null || price != null ||
            comparePrice != null || costPrice != null || weight != null ||
            productAttributes != null || visibility != null || taxClass != null ||
            metaTitle != null || metaDescription != null || searchKeywords != null ||
            isFeatured != null || initialStock != null || lowStockThreshold != null ||
            isTrackingEnabled != null || isBackorderAllowed != null ||
            minOrderQuantity != null || maxOrderQuantity != null ||
            reorderPoint != null || reorderQuantity != null || locationCode != null;
    }

}
