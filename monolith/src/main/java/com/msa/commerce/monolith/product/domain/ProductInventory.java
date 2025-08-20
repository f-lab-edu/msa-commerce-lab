package com.msa.commerce.monolith.product.domain;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductInventory {

    private Long id;

    private Long productId;

    private Long productVariantId;        // nullable

    private Integer availableQuantity;    // 판매 가능 재고

    private Integer reservedQuantity;     // 예약된 재고 (주문 대기)

    private Integer totalQuantity;        // 전체 재고

    private Integer lowStockThreshold;    // 재고 부족 임계값

    private Boolean isTrackingEnabled;    // 재고 추적 여부

    private Boolean isBackorderAllowed;   // 품절 시 주문 허용 여부

    // Enhanced inventory management fields
    private Integer minOrderQuantity;     // 최소 주문 수량

    private Integer maxOrderQuantity;     // 최대 주문 수량 (nullable)

    private Integer reorderPoint;         // 재주문 임계점

    private Integer reorderQuantity;      // 재주문 수량

    private String locationCode;          // 창고 위치 코드

    private Long versionNumber;           // 낙관적 잠금용 버전 번호

    private LocalDateTime lastUpdatedAt;

    private LocalDateTime createdAt;

    @Builder
    public ProductInventory(Long productId, Long productVariantId, Integer availableQuantity,
        Integer reservedQuantity, Integer totalQuantity, Integer lowStockThreshold,
        Boolean isTrackingEnabled, Boolean isBackorderAllowed,
        Integer minOrderQuantity, Integer maxOrderQuantity, Integer reorderPoint,
        Integer reorderQuantity, String locationCode) {
        validateInventory(productId, availableQuantity, reservedQuantity, totalQuantity);
        validateEnhancedFields(minOrderQuantity, maxOrderQuantity, reorderPoint, reorderQuantity, locationCode);

        this.productId = productId;
        this.productVariantId = productVariantId;
        this.availableQuantity = availableQuantity != null ? availableQuantity : 0;
        this.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
        this.totalQuantity = totalQuantity != null ? totalQuantity : 0;
        this.lowStockThreshold = lowStockThreshold != null ? lowStockThreshold : 10;
        this.isTrackingEnabled = isTrackingEnabled != null ? isTrackingEnabled : true;
        this.isBackorderAllowed = isBackorderAllowed != null ? isBackorderAllowed : false;
        
        // Enhanced inventory management fields
        this.minOrderQuantity = minOrderQuantity != null ? minOrderQuantity : 1;
        this.maxOrderQuantity = maxOrderQuantity;
        this.reorderPoint = reorderPoint != null ? reorderPoint : 0;
        this.reorderQuantity = reorderQuantity != null ? reorderQuantity : 0;
        this.locationCode = locationCode != null ? locationCode : "MAIN";
        this.versionNumber = 0L;
        
        this.lastUpdatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    private void validateInventory(Long productId, Integer availableQuantity,
        Integer reservedQuantity, Integer totalQuantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        int available = availableQuantity != null ? availableQuantity : 0;
        int reserved = reservedQuantity != null ? reservedQuantity : 0;
        int total = totalQuantity != null ? totalQuantity : 0;

        if (available < 0 || reserved < 0 || total < 0) {
            throw new IllegalArgumentException("Inventory quantities cannot be negative");
        }

        if (available + reserved > total) {
            throw new IllegalArgumentException("Available + Reserved cannot exceed Total quantity");
        }
    }

    private void validateEnhancedFields(Integer minOrderQuantity, Integer maxOrderQuantity,
        Integer reorderPoint, Integer reorderQuantity, String locationCode) {
        if (minOrderQuantity != null && minOrderQuantity <= 0) {
            throw new IllegalArgumentException("Minimum order quantity must be positive");
        }

        if (maxOrderQuantity != null && minOrderQuantity != null && maxOrderQuantity < minOrderQuantity) {
            throw new IllegalArgumentException("Maximum order quantity cannot be less than minimum order quantity");
        }

        if (reorderPoint != null && reorderPoint < 0) {
            throw new IllegalArgumentException("Reorder point cannot be negative");
        }

        if (reorderQuantity != null && reorderQuantity < 0) {
            throw new IllegalArgumentException("Reorder quantity cannot be negative");
        }

        if (locationCode != null && locationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Location code cannot be empty");
        }
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to decrease must be positive");
        }

        if (this.availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient available stock");
        }

        this.availableQuantity -= quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to increase must be positive");
        }

        this.availableQuantity += quantity;
        this.totalQuantity += quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be positive");
        }

        if (this.availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient available stock for reservation");
        }

        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void releaseReserved(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to release must be positive");
        }

        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException("Not enough reserved stock to release");
        }

        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return isTrackingEnabled && availableQuantity <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return availableQuantity == 0;
    }

    public boolean isReorderNeeded() {
        return isTrackingEnabled && reorderPoint > 0 && availableQuantity <= reorderPoint;
    }

    public boolean canFulfillOrder(int requestedQuantity) {
        return canOrder(requestedQuantity) && (availableQuantity >= requestedQuantity || isBackorderAllowed);
    }

    public void adjustStock(int adjustment, String reason) {
        if (adjustment == 0) {
            return;
        }

        if (adjustment > 0) {
            increaseStock(adjustment);
        } else {
            decreaseStock(Math.abs(adjustment));
        }
    }

    public void updateVersionNumber() {
        this.versionNumber++;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public int getAvailableToReserve() {
        return Math.max(0, availableQuantity);
    }

    public int getUncommittedQuantity() {
        return totalQuantity - availableQuantity - reservedQuantity;
    }

    public boolean canOrder(int quantity) {
        if (!isTrackingEnabled) {
            return true; // 재고 추적하지 않으면 항상 주문 가능
        }

        // 최소/최대 주문 수량 검증
        if (quantity < minOrderQuantity) {
            return false;
        }

        if (maxOrderQuantity != null && quantity > maxOrderQuantity) {
            return false;
        }

        if (availableQuantity >= quantity) {
            return true;
        }

        return isBackorderAllowed; // 재고 부족 시 백오더 허용 여부에 따라
    }

    public static ProductInventory reconstitute(Long id, Long productId, Long productVariantId,
        Integer availableQuantity, Integer reservedQuantity, Integer totalQuantity, Integer lowStockThreshold,
        Boolean isTrackingEnabled, Boolean isBackorderAllowed,
        Integer minOrderQuantity, Integer maxOrderQuantity, Integer reorderPoint,
        Integer reorderQuantity, String locationCode, Long versionNumber,
        LocalDateTime lastUpdatedAt, LocalDateTime createdAt) {
        ProductInventory inventory = new ProductInventory();
        inventory.id = id;
        inventory.productId = productId;
        inventory.productVariantId = productVariantId;
        inventory.availableQuantity = availableQuantity;
        inventory.reservedQuantity = reservedQuantity;
        inventory.totalQuantity = totalQuantity;
        inventory.lowStockThreshold = lowStockThreshold;
        inventory.isTrackingEnabled = isTrackingEnabled;
        inventory.isBackorderAllowed = isBackorderAllowed;
        inventory.minOrderQuantity = minOrderQuantity;
        inventory.maxOrderQuantity = maxOrderQuantity;
        inventory.reorderPoint = reorderPoint;
        inventory.reorderQuantity = reorderQuantity;
        inventory.locationCode = locationCode;
        inventory.versionNumber = versionNumber;
        inventory.lastUpdatedAt = lastUpdatedAt;
        inventory.createdAt = createdAt;
        return inventory;
    }

}
