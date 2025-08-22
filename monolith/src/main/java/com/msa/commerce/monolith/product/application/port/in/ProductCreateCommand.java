package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.util.Arrays;

import com.msa.commerce.monolith.product.domain.ProductCategory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCreateCommand {

    private final Long categoryId;              // DB 스키마의 category_id와 일치

    private final String sku;                   // 필수 필드

    private final String name;

    private final String description;

    private final String shortDescription;

    private final String brand;

    private final String model;

    private final BigDecimal price;

    private final BigDecimal comparePrice;      // 할인 전 원가

    private final BigDecimal costPrice;         // 원가

    private final BigDecimal weight;

    private final String productAttributes;     // JSON 속성 (단순화)

    private final String visibility;            // 공개/비공개

    private final String taxClass;              // 세금 분류

    private final String metaTitle;             // SEO 제목

    private final String metaDescription;       // SEO 설명

    private final String searchKeywords;        // 검색 키워드

    private final Boolean isFeatured;           // 추천 상품 여부

    // 재고 관련 (별도 ProductInventory로 관리)
    private final Integer initialStock;         // 초기 재고 수량

    private final Integer lowStockThreshold;    // 재고 부족 임계값

    private final Boolean isTrackingEnabled;   // 재고 추적 여부

    private final Boolean isBackorderAllowed;  // 품절 시 주문 허용 여부

    // 확장된 재고 관리 필드
    private final Integer minOrderQuantity;     // 최소 주문 수량

    private final Integer maxOrderQuantity;     // 최대 주문 수량

    private final Integer reorderPoint;         // 재주문 임계점

    private final Integer reorderQuantity;      // 재주문 수량

    private final String locationCode;          // 창고 위치 코드

    public void validate() {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID is required.");
        }

        // 유효한 카테고리 ID인지 검증
        boolean isValidCategory = Arrays.stream(ProductCategory.values())
            .anyMatch(category -> category.getId().equals(categoryId));
        if (!isValidCategory) {
            throw new IllegalArgumentException("Invalid category ID: " + categoryId);
        }

        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        if (sku.length() > 100) {
            throw new IllegalArgumentException("SKU cannot exceed 100 characters.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name cannot exceed 255 characters.");
        }

        if (price == null) {
            throw new IllegalArgumentException("Price is required.");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        if (price.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("Price cannot exceed 99,999,999.99.");
        }

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

        if (initialStock != null && initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }

        if (lowStockThreshold != null && lowStockThreshold < 0) {
            throw new IllegalArgumentException("Low stock threshold cannot be negative.");
        }

        // 확장된 재고 필드 유효성 검증
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

}
