package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderDomainMapper {

    List<OrderItem> toOrderItemDomainList(List<OrderItemJpaEntity> entities);

    @Mapping(source = "id", target = "orderItemId")
    OrderItem toOrderItemDomain(OrderItemJpaEntity entity);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "userId", target = "customerId")
    @Mapping(source = "orderItems", target = "orderItems")
    Order toDomain(OrderJpaEntity entity);

    default UUID map(Long value) {
        return value == null ? null : new UUID(0L, value);
    }

    default Long map(UUID value) {
        return value == null ? null : value.getLeastSignificantBits();
    }

}
