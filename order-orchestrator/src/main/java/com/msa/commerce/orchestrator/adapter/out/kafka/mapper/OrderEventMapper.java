package com.msa.commerce.orchestrator.adapter.out.kafka.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderEvent;
import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderItemEvent;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {

    @Mapping(target = "eventId", expression = "java(generateEventId())")
    @Mapping(target = "eventType", constant = "ORDER_CREATED")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "orderStatus", source = "status")
    OrderEvent toOrderCreatedEvent(Order order);

    @Mapping(target = "eventId", expression = "java(generateEventId())")
    @Mapping(target = "eventType", constant = "ORDER_STATUS_CHANGED")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "orderStatus", source = "status")
    OrderEvent toOrderStatusChangedEvent(Order order);

    OrderItemEvent toOrderItemEvent(OrderItem orderItem);

    List<OrderItemEvent> toOrderItemEvents(List<OrderItem> orderItems);

    default String generateEventId() {
        return UUID.randomUUID().toString();
    }

}
