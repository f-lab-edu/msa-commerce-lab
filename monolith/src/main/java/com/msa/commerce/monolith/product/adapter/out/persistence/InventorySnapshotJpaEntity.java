package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;

import com.msa.commerce.monolith.product.domain.StockStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// total_quantity, stock_status는 DB generated column이라 읽기 전용으로 매핑한다.
@Entity
@Table(name = "inventory_snapshots")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventorySnapshotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "location_code", nullable = false, length = 50)
    private String locationCode;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "total_quantity", insertable = false, updatable = false)
    private Integer totalQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", insertable = false, updatable = false, length = 20)
    private StockStatus stockStatus;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false)
    private Long version;

}
