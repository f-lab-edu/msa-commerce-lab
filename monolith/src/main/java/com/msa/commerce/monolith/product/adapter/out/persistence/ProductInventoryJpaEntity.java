package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.monolith.product.domain.ProductInventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_inventories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProductInventoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_variant_id")
    private Long productVariantId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "is_tracking_enabled", nullable = false)
    private Boolean isTrackingEnabled;

    @Column(name = "is_backorder_allowed", nullable = false)
    private Boolean isBackorderAllowed;

    // Enhanced inventory management fields
    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity;

    @Column(name = "max_order_quantity")
    private Integer maxOrderQuantity;

    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;

    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity;

    @Column(name = "location_code", nullable = false, length = 20)
    private String locationCode;

    @jakarta.persistence.Version
    @Column(name = "version_number", nullable = false)
    private Long versionNumber;

    @LastModifiedDate
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ProductInventoryJpaEntity fromDomainEntity(ProductInventory inventory) {
        ProductInventoryJpaEntity jpaEntity = new ProductInventoryJpaEntity();
        jpaEntity.id = inventory.getId();
        jpaEntity.productId = inventory.getProductId();
        jpaEntity.productVariantId = inventory.getProductVariantId();
        jpaEntity.availableQuantity = inventory.getAvailableQuantity();
        jpaEntity.reservedQuantity = inventory.getReservedQuantity();
        jpaEntity.totalQuantity = inventory.getTotalQuantity();
        jpaEntity.lowStockThreshold = inventory.getLowStockThreshold();
        jpaEntity.isTrackingEnabled = inventory.getIsTrackingEnabled();
        jpaEntity.isBackorderAllowed = inventory.getIsBackorderAllowed();
        jpaEntity.minOrderQuantity = inventory.getMinOrderQuantity();
        jpaEntity.maxOrderQuantity = inventory.getMaxOrderQuantity();
        jpaEntity.reorderPoint = inventory.getReorderPoint();
        jpaEntity.reorderQuantity = inventory.getReorderQuantity();
        jpaEntity.locationCode = inventory.getLocationCode();
        jpaEntity.versionNumber = inventory.getVersionNumber();
        jpaEntity.lastUpdatedAt = inventory.getLastUpdatedAt();
        jpaEntity.createdAt = inventory.getCreatedAt();
        return jpaEntity;
    }

    public static ProductInventoryJpaEntity fromDomainEntityForCreation(ProductInventory inventory) {
        ProductInventoryJpaEntity jpaEntity = new ProductInventoryJpaEntity();
        jpaEntity.productId = inventory.getProductId();
        jpaEntity.productVariantId = inventory.getProductVariantId();
        jpaEntity.availableQuantity = inventory.getAvailableQuantity();
        jpaEntity.reservedQuantity = inventory.getReservedQuantity();
        jpaEntity.totalQuantity = inventory.getTotalQuantity();
        jpaEntity.lowStockThreshold = inventory.getLowStockThreshold();
        jpaEntity.isTrackingEnabled = inventory.getIsTrackingEnabled();
        jpaEntity.isBackorderAllowed = inventory.getIsBackorderAllowed();
        jpaEntity.minOrderQuantity = inventory.getMinOrderQuantity();
        jpaEntity.maxOrderQuantity = inventory.getMaxOrderQuantity();
        jpaEntity.reorderPoint = inventory.getReorderPoint();
        jpaEntity.reorderQuantity = inventory.getReorderQuantity();
        jpaEntity.locationCode = inventory.getLocationCode();
        jpaEntity.versionNumber = inventory.getVersionNumber();
        return jpaEntity;
    }

    public ProductInventory toDomainEntity() {
        return ProductInventory.reconstitute(
            this.id,
            this.productId,
            this.productVariantId,
            this.availableQuantity,
            this.reservedQuantity,
            this.totalQuantity,
            this.lowStockThreshold,
            this.isTrackingEnabled,
            this.isBackorderAllowed,
            this.minOrderQuantity,
            this.maxOrderQuantity,
            this.reorderPoint,
            this.reorderQuantity,
            this.locationCode,
            this.versionNumber,
            this.lastUpdatedAt,
            this.createdAt
        );
    }

}
