package com.msa.commerce.monolith.product.adapter.in.web;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @NotNull(message = "Category ID is required.")
    @Positive(message = "Category ID must be positive.")
    private Long categoryId;

    @NotBlank(message = "SKU is required.")
    @Size(max = 100, message = "SKU cannot exceed 100 characters.")
    private String sku;

    @NotBlank(message = "Product name is required.")
    @Size(max = 255, message = "Product name cannot exceed 255 characters.")
    private String name;

    @Size(max = 5000, message = "Product description cannot exceed 5000 characters.")
    private String description;

    @Size(max = 500, message = "Short description cannot exceed 500 characters.")
    private String shortDescription;

    @Size(max = 100, message = "Brand cannot exceed 100 characters.")
    private String brand;

    @Size(max = 100, message = "Model cannot exceed 100 characters.")
    private String model;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.")
    @DecimalMax(value = "99999999.99", message = "Price cannot exceed 99,999,999.99.")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format.")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Compare price must be greater than 0.")
    @Digits(integer = 8, fraction = 2, message = "Invalid compare price format.")
    private BigDecimal comparePrice;

    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0.")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format.")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0.")
    @Digits(integer = 8, fraction = 2, message = "Invalid weight format.")
    private BigDecimal weight;

    private String productAttributes;

    @Size(max = 20, message = "Visibility cannot exceed 20 characters.")
    private String visibility;

    @Size(max = 50, message = "Tax class cannot exceed 50 characters.")
    private String taxClass;

    @Size(max = 255, message = "Meta title cannot exceed 255 characters.")
    private String metaTitle;

    @Size(max = 500, message = "Meta description cannot exceed 500 characters.")
    private String metaDescription;

    @Size(max = 1000, message = "Search keywords cannot exceed 1000 characters.")
    private String searchKeywords;

    private Boolean isFeatured;

    // 재고 관련 필드 (별도 도메인으로 관리)
    @Min(value = 0, message = "Initial stock cannot be negative.")
    private Integer initialStock;

    @Min(value = 0, message = "Low stock threshold cannot be negative.")
    private Integer lowStockThreshold;

    private Boolean isTrackingEnabled;

    private Boolean isBackorderAllowed;

    // 확장된 재고 관리 필드
    @Min(value = 1, message = "Minimum order quantity must be at least 1.")
    private Integer minOrderQuantity;

    @Min(value = 1, message = "Maximum order quantity must be at least 1.")
    private Integer maxOrderQuantity;

    @Min(value = 0, message = "Reorder point cannot be negative.")
    private Integer reorderPoint;

    @Min(value = 0, message = "Reorder quantity cannot be negative.")
    private Integer reorderQuantity;

    @Size(max = 50, message = "Location code cannot exceed 50 characters.")
    private String locationCode;
}