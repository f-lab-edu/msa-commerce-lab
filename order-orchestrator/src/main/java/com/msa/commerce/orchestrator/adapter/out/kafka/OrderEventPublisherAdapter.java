package com.msa.commerce.orchestrator.adapter.out.kafka;

import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.application.port.out.EventPublisher;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.event.OrderCreatedEvent;
import com.msa.commerce.orchestrator.domain.event.OrderEventMapper;
import com.msa.commerce.orchestrator.domain.event.OrderUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisherAdapter implements OrderEventPublisher {

    private final EventPublisher eventPublisher;

    @Override
    public void publishOrderCreated(Order order) {
        publishOrderCreated(order, null);
    }

    @Override
    public void publishOrderCreated(Order order, String correlationId) {
        log.info("Publishing OrderCreatedEvent for orderId: {}, orderNumber: {}, correlationId: {}", order.getOrderId(), order.getOrderNumber(), correlationId);

        OrderCreatedEvent event = OrderEventMapper.toOrderCreatedEvent(order, correlationId);
        eventPublisher.publish(KafkaTopics.ORDER_CREATED, event);

        log.info("Successfully queued OrderCreatedEvent for orderId: {}", order.getOrderId());
    }

    @Override
    public void publishOrderUpdated(Order order, OrderStatus previousStatus, String reason) {
        publishOrderUpdated(order, previousStatus, reason, null);
    }

    @Override
    public void publishOrderUpdated(Order order, OrderStatus previousStatus, String reason, String correlationId) {
        log.info("Publishing OrderUpdatedEvent for orderId: {}, previousStatus: {}, currentStatus: {}, correlationId: {}", order.getOrderId(), previousStatus, order.getStatus(), correlationId);

        OrderUpdatedEvent event = OrderEventMapper.toOrderUpdatedEvent(
            order, previousStatus, reason, correlationId
        );
        eventPublisher.publish(KafkaTopics.ORDER_UPDATED, event);

        log.info("Successfully queued OrderUpdatedEvent for orderId: {}", order.getOrderId());
    }

}
