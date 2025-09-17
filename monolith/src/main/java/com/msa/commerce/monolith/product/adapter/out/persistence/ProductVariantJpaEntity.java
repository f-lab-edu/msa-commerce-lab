package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.msa.commerce.monolith.product.domain.ProductVariantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "product_variants",
    indexes = {
        @Index(name = "idx_variants_product", columnList = "product_id, status"),
        @Index(name = "idx_variants_sku", columnList = "variant_sku"),
        @Index(name = "idx_variants_color", columnList = "color"),
        @Index(name = "idx_variants_size", columnList = "size")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariantJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @Column(name = "variant_sku", length = 100, unique = true, nullable = false)
    private String variantSku;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "price_adjustment", precision = 10, scale = 4)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProductVariantStatus status = ProductVariantStatus.ACTIVE;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = Boolean.FALSE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String options;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String size;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ProductVariantJpaEntity(ProductJpaEntity product, String variantSku, String name,
            BigDecimal priceAdjustment, ProductVariantStatus status, Boolean isDefault,
            String options, String color, String size) {
        this.product = product;
        this.variantSku = variantSku;
        this.name = name;
        this.priceAdjustment = priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO;
        this.status = status != null ? status : ProductVariantStatus.ACTIVE;
        this.isDefault = isDefault != null ? isDefault : Boolean.FALSE;
        this.options = options;
        this.color = color;
        this.size = size;
    }

    public void updateVariantInfo(String name, BigDecimal priceAdjustment, String options,
            String color, String size) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (priceAdjustment != null) {
            this.priceAdjustment = priceAdjustment;
        }
        if (options != null) {
            this.options = options;
        }
        if (color != null) {
            this.color = color;
        }
        if (size != null) {
            this.size = size;
        }
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductVariantStatus.INACTIVE;
    }

    public void markOutOfStock() {
        this.status = ProductVariantStatus.OUT_OF_STOCK;
    }

    public void setAsDefault() {
        this.isDefault = Boolean.TRUE;
    }

    public void unsetAsDefault() {
        this.isDefault = Boolean.FALSE;
    }

    public boolean isAvailable() {
        return status == ProductVariantStatus.ACTIVE;
    }

    public BigDecimal getEffectivePrice() {
        if (product != null && product.getBasePrice() != null) {
            return product.getBasePrice().add(priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO);
        }
        return priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO;
    }
}