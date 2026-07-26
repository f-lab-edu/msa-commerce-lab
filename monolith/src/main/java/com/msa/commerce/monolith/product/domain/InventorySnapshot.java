package com.msa.commerce.monolith.product.domain;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySnapshot {

    private static final String DEFAULT_LOCATION = "MAIN";

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private Long id;

    private Long productId;

    private Long variantId;

    private String locationCode;

    private int availableQuantity;

    private int reservedQuantity;

    private int lowStockThreshold;

    private LocalDateTime lastUpdatedAt;

    private Long version;

    public static InventorySnapshot create(Long productId, Long variantId, String locationCode,
        Integer lowStockThreshold) {
        requireProductId(productId);
        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.productId = productId;
        snapshot.variantId = variantId;
        snapshot.locationCode = locationCode != null ? locationCode : DEFAULT_LOCATION;
        snapshot.lowStockThreshold = lowStockThreshold != null ? lowStockThreshold : DEFAULT_LOW_STOCK_THRESHOLD;
        snapshot.lastUpdatedAt = LocalDateTime.now();
        snapshot.version = 1L;
        return snapshot;
    }

    public static InventorySnapshot reconstitute(Long id, Long productId, Long variantId,
        String locationCode, int availableQuantity, int reservedQuantity, int lowStockThreshold,
        LocalDateTime lastUpdatedAt, Long version) {
        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.id = id;
        snapshot.productId = productId;
        snapshot.variantId = variantId;
        snapshot.locationCode = locationCode;
        snapshot.availableQuantity = availableQuantity;
        snapshot.reservedQuantity = reservedQuantity;
        snapshot.lowStockThreshold = lowStockThreshold;
        snapshot.lastUpdatedAt = lastUpdatedAt;
        snapshot.version = version;
        return snapshot;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
    }

    private static void requireProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
    }

    public void stockIn(int quantity) {
        requirePositive(quantity);
        applyAvailableChange(quantity);
    }

    public void stockOut(int quantity) {
        requirePositive(quantity);
        requireAvailable(quantity);
        applyAvailableChange(-quantity);
    }

    public void reserve(int quantity) {
        requirePositive(quantity);
        requireAvailable(quantity);
        this.reservedQuantity += quantity;
        applyAvailableChange(-quantity);
    }

    public void releaseReservation(int quantity) {
        requirePositive(quantity);
        requireReserved(quantity);
        this.reservedQuantity -= quantity;
        applyAvailableChange(quantity);
    }

    // 예약 확정은 예약분이 출고로 확정되는 것이므로 가용 재고는 변하지 않는다.
    public void confirmReservation(int quantity) {
        requirePositive(quantity);
        requireReserved(quantity);
        this.reservedQuantity -= quantity;
        touch();
    }

    public void adjustTo(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Adjusted quantity cannot be negative.");
        }
        applyAvailableChange(quantity - this.availableQuantity);
    }

    public int totalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public StockStatus stockStatus() {
        if (availableQuantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        return availableQuantity <= lowStockThreshold ? StockStatus.LOW_STOCK : StockStatus.IN_STOCK;
    }

    private void applyAvailableChange(int change) {
        this.availableQuantity += change;
        touch();
    }

    private void touch() {
        this.lastUpdatedAt = LocalDateTime.now();
        this.version = this.version + 1;
    }

    private void requireAvailable(int quantity) {
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(
                "Insufficient stock: available=%d, requested=%d".formatted(availableQuantity, quantity));
        }
    }

    private void requireReserved(int quantity) {
        if (reservedQuantity < quantity) {
            throw new InsufficientStockException(
                "Insufficient reserved stock: reserved=%d, requested=%d".formatted(reservedQuantity, quantity));
        }
    }

}
