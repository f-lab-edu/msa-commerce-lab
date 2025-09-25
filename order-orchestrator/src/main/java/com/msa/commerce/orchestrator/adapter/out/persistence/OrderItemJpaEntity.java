package com.msa.commerce.orchestrator.adapter.out.persistence;

import com.msa.commerce.orchestrator.domain.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for OrderItem domain model.
 * Maps to the order_items table in db_order database.
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_variant_id")
    private Long productVariantId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_sku", nullable = false, length = 100)
    private String productSku;

    @Column(name = "variant_name", length = 255)
    private String variantName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Creates OrderItemJpaEntity from OrderItem domain model
     */
    public static OrderItemJpaEntity from(OrderItem orderItem, OrderJpaEntity orderEntity) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.order = orderEntity;
        entity.productId = orderItem.getProductId();
        entity.productVariantId = orderItem.getProductVariantId();
        entity.productName = orderItem.getProductName();
        entity.productSku = orderItem.getProductSku();
        entity.variantName = orderItem.getVariantName();
        entity.quantity = orderItem.getQuantity();
        entity.unitPrice = orderItem.getUnitPrice();
        entity.totalPrice = orderItem.getTotalPrice();
        return entity;
    }

    /**
     * Converts to OrderItem domain model
     */
    public OrderItem toDomain() {
        return OrderItem.create(
            productId,
            productName,
            productSku,
            productVariantId,
            variantName,
            quantity,
            unitPrice
        );
    }

    /**
     * Updates entity from OrderItem domain model
     */
    public void updateFrom(OrderItem orderItem) {
        this.quantity = orderItem.getQuantity();
        this.unitPrice = orderItem.getUnitPrice();
        this.totalPrice = orderItem.getTotalPrice();
    }
}