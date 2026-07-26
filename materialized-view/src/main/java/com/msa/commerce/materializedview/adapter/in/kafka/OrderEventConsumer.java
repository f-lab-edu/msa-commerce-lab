package com.msa.commerce.materializedview.adapter.in.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderCreatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderUpdatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.mapper.OrderViewMapper;
import com.msa.commerce.materializedview.application.port.in.UpdateOrderViewUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final UpdateOrderViewUseCase updateOrderViewUseCase;

    private final OrderViewMapper orderViewMapper;

    @KafkaListener(
        topics = OrderEventTopics.ORDER_CREATED,
        containerFactory = "orderCreatedListenerContainerFactory"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.debug("Received order.created: orderId={}, eventId={}",
            event.orderId(), event.metadata().eventId());
        updateOrderViewUseCase.applyOrderCreated(orderViewMapper.toView(event));
    }

    @KafkaListener(
        topics = OrderEventTopics.ORDER_UPDATED,
        containerFactory = "orderUpdatedListenerContainerFactory"
    )
    public void onOrderUpdated(OrderUpdatedEvent event) {
        log.debug("Received order.updated: orderId={}, {} -> {}",
            event.orderId(), event.previousStatus(), event.currentStatus());
        updateOrderViewUseCase.applyStatusChanged(orderViewMapper.toView(event));
    }

}
