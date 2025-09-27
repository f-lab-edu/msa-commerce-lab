package com.msa.commerce.orchestrator.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "orderId")
@ToString(exclude = {"orderItems", "shippingAddress"})
public class Order {

    private UUID orderId;

    private String orderNumber;

    private Long customerId;

    private OrderStatus status;

    private BigDecimal subtotalAmount;

    private BigDecimal taxAmount;

    private BigDecimal shippingAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String currency;

    private Map<String, Object> shippingAddress;

    private LocalDateTime orderDate;

    private LocalDateTime confirmedAt;

    private LocalDateTime paymentCompletedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime cancelledAt;

    private String sourceChannel;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(UUID orderId, String orderNumber, Long customerId, OrderStatus status, BigDecimal subtotalAmount, BigDecimal taxAmount, BigDecimal shippingAmount, BigDecimal discountAmount,
        BigDecimal totalAmount, String currency, Map<String, Object> shippingAddress, LocalDateTime orderDate, LocalDateTime confirmedAt, LocalDateTime paymentCompletedAt, LocalDateTime shippedAt,
        LocalDateTime deliveredAt, LocalDateTime cancelledAt, String sourceChannel, Long version, LocalDateTime createdAt, LocalDateTime updatedAt, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.status = status;
        this.subtotalAmount = subtotalAmount;
        this.taxAmount = taxAmount;
        this.shippingAmount = shippingAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
        this.confirmedAt = confirmedAt;
        this.paymentCompletedAt = paymentCompletedAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.cancelledAt = cancelledAt;
        this.sourceChannel = sourceChannel;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.orderItems = orderItems;
    }

    private Order(UUID orderId, String orderNumber, Long customerId,
        Map<String, Object> shippingAddress, String sourceChannel) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.subtotalAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.shippingAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.currency = "KRW";
        this.shippingAddress = new HashMap<>(shippingAddress);
        this.orderDate = LocalDateTime.now();
        this.sourceChannel = sourceChannel != null ? sourceChannel : "WEB";
        this.version = 1L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.orderItems = new ArrayList<>();
    }

    public static Order create(String orderNumber, Long customerId,
        Map<String, Object> shippingAddress, String sourceChannel) {
        validateCreationParameters(orderNumber, customerId, shippingAddress);

        return new Order(
            UUID.randomUUID(),
            orderNumber,
            customerId,
            shippingAddress,
            sourceChannel
        );
    }

    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }

        if (!status.canBeCancelled()) {
            throw new IllegalStateException("Cannot add items to order in status: " + status);
        }

        this.orderItems.add(orderItem);
        recalculateAmounts();
    }

    public void removeOrderItem(UUID orderItemId) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("Order item ID cannot be null");
        }

        if (!status.canBeCancelled()) {
            throw new IllegalStateException("Cannot remove items from order in status: " + status);
        }

        boolean removed = orderItems.removeIf(item -> item.getOrderItemId().equals(orderItemId));
        if (removed) {
            recalculateAmounts();
        }
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be in PENDING status to be confirmed");
        }

        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm order with no items");
        }

        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaymentPending() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before payment can be pending");
        }

        this.status = OrderStatus.PAYMENT_PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaymentCompleted() {
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Payment must be pending before it can be completed");
        }

        this.status = OrderStatus.PAID;
        this.paymentCompletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void startProcessing() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Order must be paid before processing can start");
        }

        this.status = OrderStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markShipped() {
        if (status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Order must be processing before it can be shipped");
        }

        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markDelivered() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be shipped before it can be delivered");
        }

        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + status);
        }

        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateShippingAmount(BigDecimal shippingAmount) {
        if (shippingAmount == null || shippingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Shipping amount cannot be null or negative");
        }

        this.shippingAmount = shippingAmount;
        recalculateAmounts();
    }

    public void updateTaxAmount(BigDecimal taxAmount) {
        if (taxAmount == null || taxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax amount cannot be null or negative");
        }

        this.taxAmount = taxAmount;
        recalculateAmounts();
    }

    public void updateDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount cannot be null or negative");
        }

        this.discountAmount = discountAmount;
        recalculateAmounts();
    }

    private void recalculateAmounts() {
        this.subtotalAmount = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotalAmount
            .add(taxAmount)
            .add(shippingAmount)
            .subtract(discountAmount);

        this.updatedAt = LocalDateTime.now();
    }

    public int getTotalItemCount() {
        return orderItems.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }

    private static void validateCreationParameters(String orderNumber, Long customerId,
        Map<String, Object> shippingAddress) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (shippingAddress == null || shippingAddress.isEmpty()) {
            throw new IllegalArgumentException("Shipping address cannot be null or empty");
        }
    }

    public Map<String, Object> getShippingAddress() {
        return Collections.unmodifiableMap(shippingAddress);
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

}
