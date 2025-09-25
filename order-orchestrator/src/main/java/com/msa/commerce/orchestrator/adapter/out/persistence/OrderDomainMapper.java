package com.msa.commerce.orchestrator.adapter.out.persistence;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert between JPA entities and domain models.
 * Handles the conversion logic for Order and OrderItem.
 */
@Component
public class OrderDomainMapper {

    /**
     * Converts OrderJpaEntity to Order domain model
     * Note: This is a temporary implementation. In a real scenario, we would need
     * a proper reconstitution method in the domain model or use a more sophisticated
     * mapping strategy that can handle private constructors and immutable objects.
     */
    public Order toDomain(OrderJpaEntity entity) {
        // For now, we'll create a new order and then manually set the state
        // This is not ideal but works for the MVP implementation
        Order order = Order.create(
            entity.getOrderNumber(),
            entity.getUserId(),
            entity.getShippingAddress(),
            entity.getSourceChannel()
        );

        // Manually set the internal state using reflection or create a builder pattern
        // For now, this will throw UnsupportedOperationException
        // TODO: Implement proper domain reconstitution
        throw new UnsupportedOperationException(
            "Domain reconstitution not yet implemented. Need to add reconstitute method to Order domain model " +
            "or implement a builder pattern that can handle existing data."
        );
    }

    /**
     * Converts list of OrderItemJpaEntity to list of OrderItem domain models
     */
    public List<OrderItem> toOrderItemDomainList(List<OrderItemJpaEntity> entities) {
        return entities.stream()
                .map(this::toOrderItemDomain)
                .collect(Collectors.toList());
    }

    /**
     * Converts OrderItemJpaEntity to OrderItem domain model
     */
    public OrderItem toOrderItemDomain(OrderItemJpaEntity entity) {
        return OrderItem.create(
            entity.getProductId(),
            entity.getProductName(),
            entity.getProductSku(),
            entity.getProductVariantId(),
            entity.getVariantName(),
            entity.getQuantity(),
            entity.getUnitPrice()
        );
    }
}