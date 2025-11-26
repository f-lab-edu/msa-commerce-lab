package com.msa.commerce.orchestrator.adapter.out.crossdomain;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.service.CrossDomainEventService;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.crossdomain.DomainType;
import com.msa.commerce.orchestrator.domain.crossdomain.EntityType;
import com.msa.commerce.orchestrator.domain.crossdomain.EventType;
import com.msa.commerce.orchestrator.domain.event.OrderCreatedEvent;
import com.msa.commerce.orchestrator.domain.event.OrderEventMapper;
import com.msa.commerce.orchestrator.domain.event.OrderUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class CrossDomainOrderEventPublisher implements OrderEventPublisher {

    private static final DomainType SOURCE_DOMAIN = DomainType.ORDER_ORCHESTRATOR;

    private static final EntityType ENTITY_TYPE = EntityType.ORDER;

    private final CrossDomainEventService crossDomainEventService;

    @Override
    public void publishOrderCreated(Order order) {
        publishOrderCreated(order, null);
    }

    @Override
    public void publishOrderCreated(Order order, String correlationId) {
        OrderCreatedEvent event = OrderEventMapper.toOrderCreatedEvent(order, correlationId);

        crossDomainEventService.saveEvent(
            EventType.ORDER_CREATED,
            SOURCE_DOMAIN,
            List.of(DomainType.PAYMENT, DomainType.INVENTORY),
            ENTITY_TYPE,
            order.getOrderId().toString(),
            null,
            event,
            correlationId,
            KafkaTopics.ORDER_CREATED
        );
    }

    @Override
    public void publishOrderUpdated(Order order, OrderStatus previousStatus, String reason) {
        publishOrderUpdated(order, previousStatus, reason, null);
    }

    @Override
    public void publishOrderUpdated(Order order, OrderStatus previousStatus, String reason, String correlationId) {
        OrderUpdatedEvent event = OrderEventMapper.toOrderUpdatedEvent(
            order, previousStatus, reason, correlationId
        );

        crossDomainEventService.saveEvent(
            EventType.ORDER_UPDATED,
            SOURCE_DOMAIN,
            List.of(DomainType.PAYMENT, DomainType.INVENTORY, DomainType.MATERIALIZED_VIEW),
            ENTITY_TYPE,
            order.getOrderId().toString(),
            null,
            event,
            correlationId,
            KafkaTopics.ORDER_UPDATED
        );
    }

}
