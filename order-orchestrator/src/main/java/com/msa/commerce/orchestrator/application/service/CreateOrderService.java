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

        orderEventPublisher.publishOrderCreated(savedOrder);

        return savedOrder.getOrderId();
    }

}
