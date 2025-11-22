package com.msa.commerce.orchestrator.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        log.info("Creating order for customer: {}, orderNumber: {}", command.getCustomerId(), command.getOrderNumber());

        Order order = Order.create(
            command.getOrderNumber(),
            command.getCustomerId(),
            command.getShippingAddress(),
            command.getSourceChannel()
        );

        command.getOrderItems().forEach(itemCommand -> {
            OrderItem orderItem = OrderItem.create(
                itemCommand.getProductId(),
                itemCommand.getProductName(),
                itemCommand.getProductSku(),
                itemCommand.getProductVariantId(),
                itemCommand.getVariantName(),
                itemCommand.getQuantity(),
                itemCommand.getUnitPrice()
            );
            order.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: orderId={}, orderNumber={}", savedOrder.getOrderId(), savedOrder.getOrderNumber());

        try {
            orderEventPublisher.publishOrderCreated(savedOrder);

            log.info("OrderCreatedEvent published successfully for orderId: {}", savedOrder.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for orderId: {}, error: {}", savedOrder.getOrderId(), e.getMessage(), e);

            throw new OrderEventPublishFailedException("Failed to publish order created event for orderId: " + savedOrder.getOrderId(), e);
        }

        return savedOrder.getOrderId();
    }

}
