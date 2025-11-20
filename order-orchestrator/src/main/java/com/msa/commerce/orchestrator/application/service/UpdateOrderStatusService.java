package com.msa.commerce.orchestrator.application.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrderStatusService.class);

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus, String reason) {
        log.info("Updating order status: orderId={}, newStatus={}, reason={}",
            orderId, newStatus, reason);

        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        OrderStatus previousStatus = order.getStatus();

        if (previousStatus == newStatus) {
            log.warn("Order status is already {}, skipping update for orderId: {}",
                newStatus, orderId);
            return;
        }

        updateOrderStatusByType(order, newStatus);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: orderId={}, previousStatus={}, currentStatus={}",
            updatedOrder.getOrderId(), previousStatus, updatedOrder.getStatus());

        try {
            orderEventPublisher.publishOrderUpdated(updatedOrder, previousStatus, reason);
            log.info("OrderUpdatedEvent published successfully for orderId: {}", updatedOrder.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderUpdatedEvent for orderId: {}, error: {}",
                updatedOrder.getOrderId(), e.getMessage(), e);
            throw new OrderEventPublishFailedException(
                "Failed to publish order updated event for orderId: " + updatedOrder.getOrderId(), e
            );
        }
    }

    private void updateOrderStatusByType(Order order, OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED -> order.confirm();
            case PAYMENT_PENDING -> order.markPaymentPending();
            case PAID -> order.markPaymentCompleted();
            case PROCESSING -> order.startProcessing();
            case SHIPPED -> order.markShipped();
            case DELIVERED -> order.markDelivered();
            case CANCELLED -> order.cancel();
            case PENDING, REFUNDED, FAILED -> throw new IllegalArgumentException(
                "Cannot directly transition to status: " + newStatus
            );
        }
    }

}
