package com.msa.commerce.monolith.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    private Long id;

    private String sku;

    private String name;

    private String shortDescription;

    private String description;

    private Long categoryId;

    private String brand;

    private ProductType productType;

    private ProductStatus status;

    private BigDecimal basePrice;

    private BigDecimal salePrice;

    private String currency;

    private Integer weightGrams;

    private Boolean requiresShipping;

    private Boolean isTaxable;

    private Boolean isFeatured;

    private String slug;

    private String searchTags;

    private String primaryImageUrl;

    private Integer minOrderQuantity;

    private Integer maxOrderQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Long version;

    @Builder
    public Product(String sku, String name, String shortDescription, String description,
        Long categoryId, String brand, ProductType productType, BigDecimal basePrice,
        BigDecimal salePrice, String currency, Integer weightGrams, Boolean requiresShipping,
        Boolean isTaxable, Boolean isFeatured, String slug, String searchTags,
        String primaryImageUrl, Integer minOrderQuantity, Integer maxOrderQuantity) {
        validateProduct(sku, name, basePrice);

        this.sku = sku;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.categoryId = categoryId;
        this.brand = brand;
        this.productType = productType != null ? productType : ProductType.PHYSICAL;
        this.status = ProductStatus.DRAFT;
        this.basePrice = basePrice;
        this.salePrice = salePrice;
        this.currency = currency != null ? currency : "KRW";
        this.weightGrams = weightGrams;
        this.requiresShipping = requiresShipping != null ? requiresShipping : true;
        this.isTaxable = isTaxable != null ? isTaxable : true;
        this.isFeatured = isFeatured != null ? isFeatured : false;
        this.slug = slug;
        this.searchTags = searchTags;
        this.primaryImageUrl = primaryImageUrl;
        this.minOrderQuantity = minOrderQuantity != null ? minOrderQuantity : 1;
        this.maxOrderQuantity = maxOrderQuantity != null ? maxOrderQuantity : 100;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1L;
    }

    public static Product reconstitute(Long id, String sku, String name, String shortDescription,
        String description, Long categoryId, String brand, ProductType productType,
        ProductStatus status, BigDecimal basePrice, BigDecimal salePrice, String currency,
        Integer weightGrams, Boolean requiresShipping, Boolean isTaxable, Boolean isFeatured,
        String slug, String searchTags, String primaryImageUrl, Integer minOrderQuantity,
        Integer maxOrderQuantity, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Long version) {
        Product product = new Product();
        product.id = id;
        product.sku = sku;
        product.name = name;
        product.shortDescription = shortDescription;
        product.description = description;
        product.categoryId = categoryId;
        product.brand = brand;
        product.productType = productType;
        product.status = status;
        product.basePrice = basePrice;
        product.salePrice = salePrice;
        product.currency = currency;
        product.weightGrams = weightGrams;
        product.requiresShipping = requiresShipping;
        product.isTaxable = isTaxable;
        product.isFeatured = isFeatured;
        product.slug = slug;
        product.searchTags = searchTags;
        product.primaryImageUrl = primaryImageUrl;
        product.minOrderQuantity = minOrderQuantity;
        product.maxOrderQuantity = maxOrderQuantity;
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        product.deletedAt = deletedAt;
        product.version = version;
        return product;
    }

    public void archive() {
        this.status = ProductStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProductInfo(String name, String description, BigDecimal basePrice) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
            this.basePrice = basePrice;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePartially(String sku, String name, String shortDescription, String description,
        Long categoryId, String brand, ProductType productType, BigDecimal basePrice,
        BigDecimal salePrice, String currency, Integer weightGrams, Boolean requiresShipping,
        Boolean isTaxable, Boolean isFeatured, String slug, String searchTags,
        String primaryImageUrl, Integer minOrderQuantity, Integer maxOrderQuantity) {

        updateBasicFields(sku, name, shortDescription, description, categoryId);
        updateBrandAndTypeFields(brand, productType);
        updatePriceAndWeightFields(basePrice, salePrice, currency, weightGrams);
        updateShippingAndTaxFields(requiresShipping, isTaxable);
        updateContentFields(slug, searchTags, primaryImageUrl);
        updateFeatureFlag(isFeatured);
        updateOrderQuantityFields(minOrderQuantity, maxOrderQuantity);

        this.updatedAt = LocalDateTime.now();
    }

    private void updateBasicFields(String sku, String name, String shortDescription, String description,
        Long categoryId) {
        updateFieldIfNotNull(sku, value -> this.sku = value);
        updateFieldIfNotNull(name, value -> this.name = value);
        updateFieldIfNotNull(shortDescription, value -> this.shortDescription = value);
        updateFieldIfNotNull(description, value -> this.description = value);
        updateFieldIfNotNull(categoryId, value -> this.categoryId = value);
    }

    private void updateBrandAndTypeFields(String brand, ProductType productType) {
        updateFieldIfNotNull(brand, value -> this.brand = value);
        updateFieldIfNotNull(productType, value -> this.productType = value);
    }

    private void updatePriceAndWeightFields(BigDecimal basePrice, BigDecimal salePrice, String currency,
        Integer weightGrams) {
        updateFieldIfNotNull(basePrice, value -> this.basePrice = value);
        updateFieldIfNotNull(salePrice, value -> this.salePrice = value);
        updateFieldIfNotNull(currency, value -> this.currency = value);
        updateFieldIfNotNull(weightGrams, value -> this.weightGrams = value);
    }

    private void updateShippingAndTaxFields(Boolean requiresShipping, Boolean isTaxable) {
        updateFieldIfNotNull(requiresShipping, value -> this.requiresShipping = value);
        updateFieldIfNotNull(isTaxable, value -> this.isTaxable = value);
    }

    private void updateContentFields(String slug, String searchTags, String primaryImageUrl) {
        updateFieldIfNotNull(slug, value -> this.slug = value);
        updateFieldIfNotNull(searchTags, value -> this.searchTags = value);
        updateFieldIfNotNull(primaryImageUrl, value -> this.primaryImageUrl = value);
    }

    private void updateFeatureFlag(Boolean isFeatured) {
        updateFieldIfNotNull(isFeatured, value -> this.isFeatured = value);
    }

    private void updateOrderQuantityFields(Integer minOrderQuantity, Integer maxOrderQuantity) {
        updateFieldIfNotNull(minOrderQuantity, value -> this.minOrderQuantity = value);
        updateFieldIfNotNull(maxOrderQuantity, value -> this.maxOrderQuantity = value);
    }

    private <T> void updateFieldIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public boolean isUpdatable() {
        return this.status != ProductStatus.ARCHIVED;
    }

    public boolean canBeUpdatedBy(String userId) {
        return isUpdatable();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void softDelete() {
        this.status = ProductStatus.ARCHIVED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isValidateOrderQuantity(Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            return false;
        }

        if (minOrderQuantity != null && requestedQuantity < minOrderQuantity) {
            return false;
        }

        if (maxOrderQuantity != null && requestedQuantity > maxOrderQuantity) {
            return false;
        }

        return true;
    }

    public Integer getMinOrderQuantity() {
        return this.minOrderQuantity != null ? this.minOrderQuantity : 1;
    }

    public Integer getMaxOrderQuantity() {
        return this.maxOrderQuantity != null ? this.maxOrderQuantity : 100;
    }

    public boolean isProductInactive() {
        return switch (status) {
            case ACTIVE -> false;
            case INACTIVE, DRAFT, ARCHIVED -> true;
        };
    }

    private void validateProduct(String sku, String name, BigDecimal basePrice) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name cannot exceed 255 characters.");
        }

        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base price must be greater than 0.");
        }

        if (basePrice.compareTo(new BigDecimal("999999999999.9999")) > 0) {
            throw new IllegalArgumentException("Base price cannot exceed 999,999,999,999.9999.");
        }
    }

    public BigDecimal getCurrnectPrice() {
        return this.salePrice != null ? this.salePrice : this.basePrice;
    }

    public BigDecimal getOriginalPrice() {
        return this.basePrice;
    }

}
