package com.msa.commerce.monolith.product.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventorySnapshot {

    private final Long id;

    private final Long productId;

    private final Long variantId;

    private final String locationCode;

    private final int availableQuantity;

    private final int reservedQuantity;

    private final int lowStockThreshold;

    private final StockStatus stockStatus;

    private final LocalDateTime lastUpdatedAt;

    private final Long version;

    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public boolean isLowStock() {
        return availableQuantity > 0 && availableQuantity <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return availableQuantity == 0;
    }

    public boolean canReserve(int quantity) {
        return availableQuantity >= quantity && quantity > 0;
    }

    public boolean canRelease(int quantity) {
        return reservedQuantity >= quantity && quantity > 0;
    }

    public StockStatus calculateStockStatus() {
        if (availableQuantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        }

        if (availableQuantity <= lowStockThreshold) {
            return StockStatus.LOW_STOCK;
        }

        return StockStatus.IN_STOCK;
    }

    public InventorySnapshot adjustQuantity(int adjustment) {
        int newAvailableQuantity = Math.max(0, availableQuantity + adjustment);

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(newAvailableQuantity)
            .reservedQuantity(this.reservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatusForQuantity(newAvailableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot adjustAvailableQuantity(int quantity) {
        if (this.availableQuantity + quantity < 0) {
            throw new IllegalArgumentException("사용 가능한 재고가 0보다 작을 수 없습니다.");
        }

        int newAvailableQuantity = availableQuantity + quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(newAvailableQuantity)
            .reservedQuantity(this.reservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatusForQuantity(newAvailableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot adjustReservedQuantity(int quantity) {
        if (this.reservedQuantity + quantity < 0) {
            throw new IllegalArgumentException("예약된 재고가 0보다 작을 수 없습니다.");
        }

        int newReservedQuantity = reservedQuantity + quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(this.availableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(this.stockStatus)
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot updateLowStockThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("재고 임계값은 0 이상이어야 합니다.");
        }

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(this.availableQuantity)
            .reservedQuantity(this.reservedQuantity)
            .lowStockThreshold(threshold)
            .stockStatus(calculateStockStatusForQuantity(this.availableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("예약 수량은 0보다 커야 합니다.");
        }
        if (this.availableQuantity < quantity) {
            throw new IllegalArgumentException("사용 가능한 재고가 부족합니다.");
        }

        int newAvailableQuantity = availableQuantity - quantity;
        int newReservedQuantity = reservedQuantity + quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(newAvailableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatusForQuantity(newAvailableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot releaseReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("해제 수량은 0보다 커야 합니다.");
        }
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("예약된 재고가 부족합니다.");
        }

        int newAvailableQuantity = availableQuantity + quantity;
        int newReservedQuantity = reservedQuantity - quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(newAvailableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatusForQuantity(newAvailableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot confirmReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("확정 수량은 0보다 커야 합니다.");
        }
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("예약된 재고가 부족합니다.");
        }

        int newReservedQuantity = reservedQuantity - quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(this.availableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(this.stockStatus)
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot reserveQuantity(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalArgumentException(
                String.format("Cannot reserve %d items. Available: %d", quantity, availableQuantity));
        }

        int newAvailableQuantity = availableQuantity - quantity;
        int newReservedQuantity = reservedQuantity + quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(newAvailableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatusForQuantity(newAvailableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot releaseQuantity(int quantity) {
        if (!canRelease(quantity)) {
            throw new IllegalArgumentException(
                String.format("Cannot release %d items. Reserved: %d", quantity, reservedQuantity));
        }

        int newAvailableQuantity = availableQuantity + quantity;
        int newReservedQuantity = reservedQuantity - quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(newAvailableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(calculateStockStatusForQuantity(newAvailableQuantity))
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    public InventorySnapshot confirmReservation(int quantity) {
        if (!canRelease(quantity)) {
            throw new IllegalArgumentException(
                String.format("Cannot confirm %d items. Reserved: %d", quantity, reservedQuantity));
        }

        int newReservedQuantity = reservedQuantity - quantity;

        return InventorySnapshot.builder()
            .id(this.id)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .availableQuantity(this.availableQuantity)
            .reservedQuantity(newReservedQuantity)
            .lowStockThreshold(this.lowStockThreshold)
            .stockStatus(this.stockStatus)
            .lastUpdatedAt(LocalDateTime.now())
            .version(this.version)
            .build();
    }

    private StockStatus calculateStockStatusForQuantity(int quantity) {
        if (quantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        }

        if (quantity <= lowStockThreshold) {
            return StockStatus.LOW_STOCK;
        }

        return StockStatus.IN_STOCK;
    }

}
