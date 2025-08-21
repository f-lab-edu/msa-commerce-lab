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

    // 부분 업데이트를 위한 새로운 메소드
    public void updatePartially(Long categoryId, String sku, String name, String description,
                              String shortDescription, String brand, String model, BigDecimal price,
                              BigDecimal comparePrice, BigDecimal costPrice, BigDecimal weight,
                              String productAttributes, String visibility, String taxClass,
                              String metaTitle, String metaDescription, String searchKeywords,
                              Boolean isFeatured) {
        
        // null이 아닌 필드들만 업데이트
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
        if (sku != null) {
            this.sku = sku;
        }
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (shortDescription != null) {
            this.shortDescription = shortDescription;
        }
        if (brand != null) {
            this.brand = brand;
        }
        if (model != null) {
            this.model = model;
        }
        if (price != null) {
            this.price = price;
        }
        if (comparePrice != null) {
            this.comparePrice = comparePrice;
        }
        if (costPrice != null) {
            this.costPrice = costPrice;
        }
        if (weight != null) {
            this.weight = weight;
        }
        if (productAttributes != null) {
            this.productAttributes = productAttributes;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
        if (taxClass != null) {
            this.taxClass = taxClass;
        }
        if (metaTitle != null) {
            this.metaTitle = metaTitle;
        }
        if (metaDescription != null) {
            this.metaDescription = metaDescription;
        }
        if (searchKeywords != null) {
            this.searchKeywords = searchKeywords;
        }
        if (isFeatured != null) {
            this.isFeatured = isFeatured;
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    // 상품이 업데이트 가능한 상태인지 확인
    public boolean isUpdatable() {
        return this.status != ProductStatus.ARCHIVED;
    }

    // 상품의 상태 변경 권한이 있는지 확인 (추후 확장 가능)
    public boolean canBeUpdatedBy(String userId) {
        // 현재는 단순히 상품이 업데이트 가능한 상태인지만 확인
        // 추후 권한 체계가 구축되면 사용자별 권한 검증 로직 추가
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
