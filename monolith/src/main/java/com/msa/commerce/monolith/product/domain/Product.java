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

    private Long categoryId;

    private String sku;

    private String name;

    private String description;

    private String shortDescription;

    private String brand;

    private String model;

    private BigDecimal price;

    private BigDecimal comparePrice;

    private BigDecimal costPrice;

    private BigDecimal weight;

    private String productAttributes;

    private ProductStatus status;

    private String visibility;

    private String taxClass;

    private String metaTitle;

    private String metaDescription;

    private String searchKeywords;

    private Boolean isFeatured;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Product(Long categoryId, String sku, String name, String description,
        String shortDescription, String brand, String model, BigDecimal price,
        BigDecimal comparePrice, BigDecimal costPrice, BigDecimal weight,
        String productAttributes, String visibility, String taxClass,
        String metaTitle, String metaDescription, String searchKeywords,
        Boolean isFeatured) {
        validateProduct(categoryId, sku, name, price);

        this.categoryId = categoryId;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.shortDescription = shortDescription;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.comparePrice = comparePrice;
        this.costPrice = costPrice;
        this.weight = weight;
        this.productAttributes = productAttributes;
        this.status = ProductStatus.DRAFT;
        this.visibility = visibility != null ? visibility : "PUBLIC";
        this.taxClass = taxClass;
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.searchKeywords = searchKeywords;
        this.isFeatured = isFeatured != null ? isFeatured : false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Product reconstitute(Long id, Long categoryId, String sku, String name, String description,
        String shortDescription, String brand, String model, BigDecimal price,
        BigDecimal comparePrice, BigDecimal costPrice, BigDecimal weight,
        String productAttributes, ProductStatus status, String visibility,
        String taxClass, String metaTitle, String metaDescription,
        String searchKeywords, Boolean isFeatured,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
        Product product = new Product();
        product.id = id;
        product.categoryId = categoryId;
        product.sku = sku;
        product.name = name;
        product.description = description;
        product.shortDescription = shortDescription;
        product.brand = brand;
        product.model = model;
        product.price = price;
        product.comparePrice = comparePrice;
        product.costPrice = costPrice;
        product.weight = weight;
        product.productAttributes = productAttributes;
        product.status = status;
        product.visibility = visibility;
        product.taxClass = taxClass;
        product.metaTitle = metaTitle;
        product.metaDescription = metaDescription;
        product.searchKeywords = searchKeywords;
        product.isFeatured = isFeatured;
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        return product;
    }

    public void archive() {
        this.status = ProductStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProductInfo(String name, String description, BigDecimal price) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            this.price = price;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePartially(Long categoryId, String sku, String name, String description,
        String shortDescription, String brand, String model, BigDecimal price,
        BigDecimal comparePrice, BigDecimal costPrice, BigDecimal weight,
        String productAttributes, String visibility, String taxClass,
        String metaTitle, String metaDescription, String searchKeywords,
        Boolean isFeatured) {

        updateBasicFields(categoryId, sku, name, description, shortDescription);
        updateBrandAndModelFields(brand, model);
        updatePriceFields(price, comparePrice, costPrice, weight);
        updateAttributesAndVisibility(productAttributes, visibility, taxClass);
        updateSeoFields(metaTitle, metaDescription, searchKeywords);
        updateFeatureFlag(isFeatured);

        this.updatedAt = LocalDateTime.now();
    }

    private void updateBasicFields(Long categoryId, String sku, String name, String description,
        String shortDescription) {
        updateFieldIfNotNull(categoryId, value -> this.categoryId = value);
        updateFieldIfNotNull(sku, value -> this.sku = value);
        updateFieldIfNotNull(name, value -> this.name = value);
        updateFieldIfNotNull(description, value -> this.description = value);
        updateFieldIfNotNull(shortDescription, value -> this.shortDescription = value);
    }

    private void updateBrandAndModelFields(String brand, String model) {
        updateFieldIfNotNull(brand, value -> this.brand = value);
        updateFieldIfNotNull(model, value -> this.model = value);
    }

    private void updatePriceFields(BigDecimal price, BigDecimal comparePrice, BigDecimal costPrice, BigDecimal weight) {
        updateFieldIfNotNull(price, value -> this.price = value);
        updateFieldIfNotNull(comparePrice, value -> this.comparePrice = value);
        updateFieldIfNotNull(costPrice, value -> this.costPrice = value);
        updateFieldIfNotNull(weight, value -> this.weight = value);
    }

    private void updateAttributesAndVisibility(String productAttributes, String visibility, String taxClass) {
        updateFieldIfNotNull(productAttributes, value -> this.productAttributes = value);
        updateFieldIfNotNull(visibility, value -> this.visibility = value);
        updateFieldIfNotNull(taxClass, value -> this.taxClass = value);
    }

    private void updateSeoFields(String metaTitle, String metaDescription, String searchKeywords) {
        updateFieldIfNotNull(metaTitle, value -> this.metaTitle = value);
        updateFieldIfNotNull(metaDescription, value -> this.metaDescription = value);
        updateFieldIfNotNull(searchKeywords, value -> this.searchKeywords = value);
    }

    private void updateFeatureFlag(Boolean isFeatured) {
        updateFieldIfNotNull(isFeatured, value -> this.isFeatured = value);
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
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateProduct(Long categoryId, String sku, String name, BigDecimal price) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID is required.");
        }

        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name cannot exceed 255 characters.");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        if (price.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("Price cannot exceed 99,999,999.99.");
        }
    }

}
