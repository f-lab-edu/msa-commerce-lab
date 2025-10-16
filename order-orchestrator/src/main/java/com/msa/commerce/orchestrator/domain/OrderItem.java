package com.msa.commerce.orchestrator.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "orderItemId")
@ToString(exclude = {"orderItemId"})
public class OrderItem {

    private UUID orderItemId;

    private Long productId;

    private String productName;

    private String productSku;

    private Long productVariantId;

    private String variantName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    @Builder
    public OrderItem(UUID orderItemId, Long productId, String productName, String productSku, Long productVariantId, String variantName, Integer quantity, BigDecimal unitPrice,
        BigDecimal totalPrice) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.productVariantId = productVariantId;
        this.variantName = variantName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    private OrderItem(UUID orderItemId, Long productId, String productName, String productSku,
        Long productVariantId, String variantName, Integer quantity,
        BigDecimal unitPrice) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.productVariantId = productVariantId;
        this.variantName = variantName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    public static OrderItem create(Long productId, String productName, String productSku,
        Long productVariantId, String variantName, Integer quantity,
        BigDecimal unitPrice) {
        validateCreationParameters(productId, productName, productSku, quantity, unitPrice);

        return new OrderItem(
            UUID.randomUUID(),
            productId,
            productName,
            productSku,
            productVariantId,
            variantName,
            quantity,
            unitPrice
        );
    }

    public void updateQuantity(Integer newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
        this.totalPrice = calculateTotalPrice();
    }

    public void updateUnitPrice(BigDecimal newUnitPrice) {
        validateUnitPrice(newUnitPrice);
        this.unitPrice = newUnitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    private BigDecimal calculateTotalPrice() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private static void validateCreationParameters(Long productId, String productName,
        String productSku, Integer quantity,
        BigDecimal unitPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (productSku == null || productSku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be null or empty");
        }
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be null or negative");
        }
    }

}
