package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_uuid", nullable = false, unique = true, length = 36)
    private UUID orderUuid;

    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 4)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount", nullable = false, precision = 12, scale = 4)
    private BigDecimal shippingAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 4)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> shippingAddress;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "source_channel", length = 50)
    private String sourceChannel;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItemJpaEntity> orderItems = new ArrayList<>();

    /**
     * Creates OrderJpaEntity from Order domain model
     */
    public static OrderJpaEntity from(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.orderUuid = order.getOrderId();
        entity.orderNumber = order.getOrderNumber();
        entity.userId = order.getCustomerId();
        entity.status = order.getStatus();
        entity.subtotalAmount = order.getSubtotalAmount();
        entity.taxAmount = order.getTaxAmount();
        entity.shippingAmount = order.getShippingAmount();
        entity.discountAmount = order.getDiscountAmount();
        entity.totalAmount = order.getTotalAmount();
        entity.currency = order.getCurrency();
        entity.shippingAddress = order.getShippingAddress();
        entity.orderDate = order.getOrderDate();
        entity.confirmedAt = order.getConfirmedAt();
        entity.paymentCompletedAt = order.getPaymentCompletedAt();
        entity.shippedAt = order.getShippedAt();
        entity.deliveredAt = order.getDeliveredAt();
        entity.cancelledAt = order.getCancelledAt();
        entity.sourceChannel = order.getSourceChannel();
        entity.version = order.getVersion();
        entity.createdAt = order.getCreatedAt();
        entity.updatedAt = order.getUpdatedAt();
        return entity;
    }

    /**
     * Converts to Order domain model
     */
    public Order toDomain() {
        // Note: This creates a new Order but we need to use reflection or builder pattern
        // to properly hydrate the domain object with existing data
        // For now, this is a placeholder - proper implementation would require
        // either making domain constructors more flexible or using a mapper
        throw new UnsupportedOperationException("Domain conversion not yet implemented - requires proper hydration strategy");
    }

    /**
     * Updates entity from Order domain model (for existing entities)
     */
    public void updateFrom(Order order) {
        this.status = order.getStatus();
        this.subtotalAmount = order.getSubtotalAmount();
        this.taxAmount = order.getTaxAmount();
        this.shippingAmount = order.getShippingAmount();
        this.discountAmount = order.getDiscountAmount();
        this.totalAmount = order.getTotalAmount();
        this.confirmedAt = order.getConfirmedAt();
        this.paymentCompletedAt = order.getPaymentCompletedAt();
        this.shippedAt = order.getShippedAt();
        this.deliveredAt = order.getDeliveredAt();
        this.cancelledAt = order.getCancelledAt();
        this.updatedAt = order.getUpdatedAt();
    }

}
