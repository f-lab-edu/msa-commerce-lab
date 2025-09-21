package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal basePrice;

    @Column(name = "sale_price", precision = 12, scale = 4)
    private BigDecimal salePrice;

    @Column(nullable = false, length = 3)
    private String currency = "KRW";

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "requires_shipping", nullable = false)
    private Boolean requiresShipping = true;

    @Column(name = "is_taxable", nullable = false)
    private Boolean isTaxable = true;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Column(name = "search_tags", columnDefinition = "TEXT")
    private String searchTags;

    @Column(name = "primary_image_url", length = 500)
    private String primaryImageUrl;

    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity = 1;

    @Column(name = "max_order_quantity", nullable = false)
    private Integer maxOrderQuantity = 100;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(nullable = false)
    private Long version = 1L;

    public static ProductJpaEntity fromDomainEntity(Product product) {
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        jpaEntity.id = product.getId();
        jpaEntity.sku = product.getSku();
        jpaEntity.name = product.getName();
        jpaEntity.shortDescription = product.getShortDescription();
        jpaEntity.description = product.getDescription();
        jpaEntity.categoryId = product.getCategoryId();
        jpaEntity.brand = product.getBrand();
        jpaEntity.productType = product.getProductType();
        jpaEntity.status = product.getStatus();
        jpaEntity.basePrice = product.getBasePrice();
        jpaEntity.salePrice = product.getSalePrice();
        jpaEntity.currency = product.getCurrency();
        jpaEntity.weightGrams = product.getWeightGrams();
        jpaEntity.requiresShipping = product.getRequiresShipping();
        jpaEntity.isTaxable = product.getIsTaxable();
        jpaEntity.isFeatured = product.getIsFeatured();
        jpaEntity.slug = product.getSlug();
        jpaEntity.searchTags = product.getSearchTags();
        jpaEntity.primaryImageUrl = product.getPrimaryImageUrl();
        jpaEntity.minOrderQuantity = product.getMinOrderQuantity();
        jpaEntity.maxOrderQuantity = product.getMaxOrderQuantity();
        jpaEntity.createdAt = product.getCreatedAt();
        jpaEntity.updatedAt = product.getUpdatedAt();
        jpaEntity.deletedAt = product.getDeletedAt();
        jpaEntity.version = product.getVersion();
        return jpaEntity;
    }

    public static ProductJpaEntity fromDomainEntityForCreation(Product product) {
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        jpaEntity.sku = product.getSku();
        jpaEntity.name = product.getName();
        jpaEntity.shortDescription = product.getShortDescription();
        jpaEntity.description = product.getDescription();
        jpaEntity.categoryId = product.getCategoryId();
        jpaEntity.brand = product.getBrand();
        jpaEntity.productType = product.getProductType();
        jpaEntity.status = product.getStatus();
        jpaEntity.basePrice = product.getBasePrice();
        jpaEntity.salePrice = product.getSalePrice();
        jpaEntity.currency = product.getCurrency();
        jpaEntity.weightGrams = product.getWeightGrams();
        jpaEntity.requiresShipping = product.getRequiresShipping();
        jpaEntity.isTaxable = product.getIsTaxable();
        jpaEntity.isFeatured = product.getIsFeatured();
        jpaEntity.slug = product.getSlug();
        jpaEntity.searchTags = product.getSearchTags();
        jpaEntity.primaryImageUrl = product.getPrimaryImageUrl();
        jpaEntity.minOrderQuantity = product.getMinOrderQuantity();
        jpaEntity.maxOrderQuantity = product.getMaxOrderQuantity();
        return jpaEntity;
    }

    public Product toDomainEntity() {
        return Product.reconstitute(
            this.id,
            this.sku,
            this.name,
            this.shortDescription,
            this.description,
            this.categoryId,
            this.brand,
            this.productType,
            this.status,
            this.basePrice,
            this.salePrice,
            this.currency,
            this.weightGrams,
            this.requiresShipping,
            this.isTaxable,
            this.isFeatured,
            this.slug,
            this.searchTags,
            this.primaryImageUrl,
            this.minOrderQuantity,
            this.maxOrderQuantity,
            this.createdAt,
            this.updatedAt,
            this.deletedAt,
            this.version
        );
    }

}
