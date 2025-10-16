package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.orchestrator.domain.OrderItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    public OrderItemJpaEntity(Long id, OrderJpaEntity order, Long productId, Long productVariantId, String productName, String productSku, String variantName, Integer quantity, BigDecimal unitPrice,
        BigDecimal totalPrice, LocalDateTime createdAt) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.productSku = productSku;
        this.variantName = variantName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

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

    public void updateFrom(OrderItem orderItem) {
        this.quantity = orderItem.getQuantity();
        this.unitPrice = orderItem.getUnitPrice();
        this.totalPrice = orderItem.getTotalPrice();
    }

}
