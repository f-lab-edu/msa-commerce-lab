package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_price", precision = 10, scale = 2)
    private BigDecimal comparePrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(name = "product_attributes", columnDefinition = "JSON")
    private String productAttributes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(length = 20)
    private String visibility;

    @Column(name = "tax_class", length = 50)
    private String taxClass;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "search_keywords", columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ProductJpaEntity fromDomainEntity(Product product) {
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        jpaEntity.id = product.getId();
        jpaEntity.categoryId = product.getCategoryId();
        jpaEntity.sku = product.getSku();
        jpaEntity.name = product.getName();
        jpaEntity.description = product.getDescription();
        jpaEntity.shortDescription = product.getShortDescription();
        jpaEntity.brand = product.getBrand();
        jpaEntity.model = product.getModel();
        jpaEntity.price = product.getPrice();
        jpaEntity.comparePrice = product.getComparePrice();
        jpaEntity.costPrice = product.getCostPrice();
        jpaEntity.weight = product.getWeight();
        jpaEntity.productAttributes = product.getProductAttributes();
        jpaEntity.status = product.getStatus();
        jpaEntity.visibility = product.getVisibility();
        jpaEntity.taxClass = product.getTaxClass();
        jpaEntity.metaTitle = product.getMetaTitle();
        jpaEntity.metaDescription = product.getMetaDescription();
        jpaEntity.searchKeywords = product.getSearchKeywords();
        jpaEntity.isFeatured = product.getIsFeatured();
        jpaEntity.createdAt = product.getCreatedAt();
        jpaEntity.updatedAt = product.getUpdatedAt();
        return jpaEntity;
    }

    public static ProductJpaEntity fromDomainEntityForCreation(Product product) {
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        jpaEntity.categoryId = product.getCategoryId();
        jpaEntity.sku = product.getSku();
        jpaEntity.name = product.getName();
        jpaEntity.description = product.getDescription();
        jpaEntity.shortDescription = product.getShortDescription();
        jpaEntity.brand = product.getBrand();
        jpaEntity.model = product.getModel();
        jpaEntity.price = product.getPrice();
        jpaEntity.comparePrice = product.getComparePrice();
        jpaEntity.costPrice = product.getCostPrice();
        jpaEntity.weight = product.getWeight();
        jpaEntity.productAttributes = product.getProductAttributes();
        jpaEntity.status = product.getStatus();
        jpaEntity.visibility = product.getVisibility();
        jpaEntity.taxClass = product.getTaxClass();
        jpaEntity.metaTitle = product.getMetaTitle();
        jpaEntity.metaDescription = product.getMetaDescription();
        jpaEntity.searchKeywords = product.getSearchKeywords();
        jpaEntity.isFeatured = product.getIsFeatured();
        return jpaEntity;
    }

    public Product toDomainEntity() {
        return Product.reconstitute(
            this.id,
            this.categoryId,
            this.sku,
            this.name,
            this.description,
            this.shortDescription,
            this.brand,
            this.model,
            this.price,
            this.comparePrice,
            this.costPrice,
            this.weight,
            this.productAttributes,
            this.status,
            this.visibility,
            this.taxClass,
            this.metaTitle,
            this.metaDescription,
            this.searchKeywords,
            this.isFeatured,
            this.createdAt,
            this.updatedAt
        );
    }

}

