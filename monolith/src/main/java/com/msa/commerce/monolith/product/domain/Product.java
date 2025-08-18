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

    private Long categoryId;              // DB 스키마와 일치

    private String sku;                   // 필수 필드 추가

    private String name;

    private String description;

    private String shortDescription;      // 추가

    private String brand;                 // 추가

    private String model;                 // 추가

    private BigDecimal price;

    private BigDecimal comparePrice;      // 할인 전 원가

    private BigDecimal costPrice;         // 원가

    private BigDecimal weight;            // 추가

    private String productAttributes;     // JSON 속성 (단순화)

    private ProductStatus status;

    private String visibility;            // 공개/비공개

    private String taxClass;              // 세금 분류

    private String metaTitle;             // SEO 제목

    private String metaDescription;       // SEO 설명

    private String searchKeywords;        // 검색 키워드

    private Boolean isFeatured;           // 추천 상품 여부

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
        this.status = ProductStatus.DRAFT; // DB 스키마 기본값과 일치
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
