package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import com.msa.commerce.monolith.product.domain.StockStatus;

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
        @Index(name = "idx_inventory_stock_status", columnList = "stock_status")
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

    @Formula("available_quantity + reserved_quantity")
    private Integer totalQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10;

    @Enumerated(EnumType.STRING)
    @Formula("""
        CASE
            WHEN available_quantity = 0 THEN 'OUT_OF_STOCK'
            WHEN available_quantity <= low_stock_threshold THEN 'LOW_STOCK'
            ELSE 'IN_STOCK'
        END
        """)
    private StockStatus stockStatus;

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

    public void adjustAvailableQuantity(int quantity) {
        if (this.availableQuantity + quantity < 0) {
            throw new IllegalArgumentException("사용 가능한 재고가 0보다 작을 수 없습니다.");
        }
        this.availableQuantity += quantity;
    }

    public void adjustReservedQuantity(int quantity) {
        if (this.reservedQuantity + quantity < 0) {
            throw new IllegalArgumentException("예약된 재고가 0보다 작을 수 없습니다.");
        }
        this.reservedQuantity += quantity;
    }

    public void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("예약 수량은 0보다 커야 합니다.");
        }
        if (this.availableQuantity < quantity) {
            throw new IllegalArgumentException("사용 가능한 재고가 부족합니다.");
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void releaseReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("해제 수량은 0보다 커야 합니다.");
        }
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("예약된 재고가 부족합니다.");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    public void confirmReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("확정 수량은 0보다 커야 합니다.");
        }
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("예약된 재고가 부족합니다.");
        }
        this.reservedQuantity -= quantity;
    }

    public void updateLowStockThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("재고 임계값은 0 이상이어야 합니다.");
        }
        this.lowStockThreshold = threshold;
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

    public StockStatus calculateStockStatus() {
        if (availableQuantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (availableQuantity <= lowStockThreshold) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.IN_STOCK;
        }
    }
}