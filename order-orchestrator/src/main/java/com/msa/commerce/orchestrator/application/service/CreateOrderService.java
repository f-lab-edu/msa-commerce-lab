package com.msa.commerce.orchestrator.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.PublishOrderEventPort;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final PublishOrderEventPort publishOrderEventPort;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        log.info("Creating order: orderNumber={}, customerId={}", command.orderNumber(), command.customerId());

        Order order = Order.create(
            command.orderNumber(),
            command.customerId(),
            command.shippingAddress(),
            command.sourceChannel()
        );

        command.orderItems().forEach(itemCommand -> {
            OrderItem orderItem = OrderItem.create(
                itemCommand.productId(),
                itemCommand.productName(),
                itemCommand.productSku(),
                itemCommand.productVariantId(),
                itemCommand.variantName(),
                itemCommand.quantity(),
                itemCommand.unitPrice()
            );
            order.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully: orderId={}, orderNumber={}", savedOrder.getOrderId(), savedOrder.getOrderNumber());

        publishOrderEventPort.publishOrderCreatedEvent(savedOrder);

        return savedOrder.getOrderId();
    }

}
