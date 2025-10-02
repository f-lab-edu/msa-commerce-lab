package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.msa.commerce.monolith.product.domain.StockStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "inventory_snapshots",
    indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_available_qty", columnList = "available_quantity"),
        @Index(name = "idx_inventory_location", columnList = "location_code")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_location", columnNames = {"product_id", "variant_id", "location_code"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySnapshotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantJpaEntity variant;

    @Column(name = "location_code", length = 50, nullable = false)
    private String locationCode = "MAIN";

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Version
    private Long version = 1L;

    @Builder
    public InventorySnapshotJpaEntity(ProductJpaEntity product, ProductVariantJpaEntity variant,
        String locationCode, Integer availableQuantity, Integer reservedQuantity,
        Integer lowStockThreshold) {
        this.product = product;
        this.variant = variant;
        this.locationCode = locationCode != null ? locationCode : "MAIN";
        this.availableQuantity = availableQuantity != null ? availableQuantity : 0;
        this.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
        this.lowStockThreshold = lowStockThreshold != null ? lowStockThreshold : 10;
    }

    // Domain conversion method
    public com.msa.commerce.monolith.product.domain.InventorySnapshot toDomain() {
        return com.msa.commerce.monolith.product.domain.InventorySnapshot.builder()
            .id(this.id)
            .productId(this.product != null ? this.product.getId() : null)
            .variantId(this.variant != null ? this.variant.getId() : null)
            .locationCode(this.locationCode)
            .availableQuantity(this.availableQuantity)
            .reservedQuantity(this.reservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatus())
            .lastUpdatedAt(this.lastUpdatedAt)
            .version(this.version)
            .build();
    }

    // Setters for updating from domain
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return availableQuantity == 0;
    }

    public boolean canReserve(int quantity) {
        return availableQuantity >= quantity;
    }

    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public StockStatus calculateStockStatus() {
        if (availableQuantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (availableQuantity <= lowStockThreshold) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.IN_STOCK;
        }
    }

    public void disableStock() {
        // 재고를 비활성화하되 수량은 유지 (감사 목적)
        // 상태는 StockStatus.DISABLED로 변경되어야 하지만,
        // 현재는 수량 정보만 유지하고 실제 사용 불가능하게 처리
    }

}
