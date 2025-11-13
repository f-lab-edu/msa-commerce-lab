package com.msa.commerce.orchestrator.adapter.out.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderEvent;
import com.msa.commerce.orchestrator.adapter.out.kafka.mapper.OrderEventMapper;
import com.msa.commerce.orchestrator.application.port.out.PublishOrderEventPort;
import com.msa.commerce.orchestrator.domain.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements PublishOrderEventPort {

    private static final String TOPIC_NAME = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderEventMapper orderEventMapper;

    @Override
    public void publishOrderCreatedEvent(Order order) {
        OrderEvent event = orderEventMapper.toOrderCreatedEvent(order);
        String key = order.getOrderId().toString();

        log.info("Publishing ORDER_CREATED event: orderId={}, eventId={}", order.getOrderId(), event.eventId());

        kafkaTemplate.send(TOPIC_NAME, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish ORDER_CREATED event: orderId={}, eventId={}",
                        order.getOrderId(), event.eventId(), ex);
                } else {
                    log.debug("Successfully published ORDER_CREATED event: orderId={}, eventId={}",
                        order.getOrderId(), event.eventId());
                }
            });
    }

    @Override
    public void publishOrderStatusChangedEvent(Order order) {
        OrderEvent event = orderEventMapper.toOrderStatusChangedEvent(order);
        String key = order.getOrderId().toString();

        log.info("Publishing ORDER_STATUS_CHANGED event: orderId={}, status={}, eventId={}",
            order.getOrderId(), order.getStatus(), event.eventId());

        kafkaTemplate.send(TOPIC_NAME, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish ORDER_STATUS_CHANGED event: orderId={}, eventId={}",
                        order.getOrderId(), event.eventId(), ex);
                } else {
                    log.debug("Successfully published ORDER_STATUS_CHANGED event: orderId={}, status={}, eventId={}",
                        order.getOrderId(), order.getStatus(), event.eventId());
                }
            });
    }
}
