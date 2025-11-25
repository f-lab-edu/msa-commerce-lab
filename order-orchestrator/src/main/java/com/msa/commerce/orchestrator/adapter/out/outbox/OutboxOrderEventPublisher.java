package com.msa.commerce.orchestrator.adapter.out.outbox;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.service.OutboxEventService;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.event.OrderCreatedEvent;
import com.msa.commerce.orchestrator.domain.event.OrderEventMapper;
import com.msa.commerce.orchestrator.domain.event.OrderUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class OutboxOrderEventPublisher implements OrderEventPublisher {

    private final OutboxEventService outboxEventService;

    @Override
    public void publishOrderCreated(Order order) {
        publishOrderCreated(order, null);
    }

    @Override
    public void publishOrderCreated(Order order, String correlationId) {
        OrderCreatedEvent event = OrderEventMapper.toOrderCreatedEvent(order, correlationId);

        outboxEventService.saveEvent(
            "Order",
            order.getOrderId().toString(),
            "OrderCreated",
            KafkaTopics.ORDER_CREATED,
            event,
            correlationId
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

        outboxEventService.saveEvent(
            "Order",
            order.getOrderId().toString(),
            "OrderUpdated",
            KafkaTopics.ORDER_UPDATED,
            event,
            correlationId
        );
    }

}
