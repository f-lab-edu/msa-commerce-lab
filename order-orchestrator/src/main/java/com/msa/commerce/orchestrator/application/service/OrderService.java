package com.msa.commerce.orchestrator.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.OrderResponse;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService implements CreateOrderUseCase {

    private static final AtomicLong ORDER_SEQUENCE = new AtomicLong(0);

    private static final DateTimeFormatter ORDER_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;

    private final OrderResponseMapper orderResponseMapper;

    @Override
    public OrderResponse createOrder(CreateOrderCommand command) {
        String orderNumber = generateOrderNumber();

        Map<String, Object> defaultShippingAddress = new HashMap<>();
        defaultShippingAddress.put("addressType", "default");

        Order order = Order.create(
            orderNumber,
            command.customerId(),
            defaultShippingAddress,
            "WEB"
        );

        command.orderItems().forEach(itemCommand -> {
            OrderItem orderItem = OrderItem.create(
                itemCommand.productId(),
                "Product-" + itemCommand.productId(),
                "SKU-" + itemCommand.productId(),
                null,
                null,
                itemCommand.quantity(),
                itemCommand.unitPrice()
            );
            order.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(order);

        return orderResponseMapper.toResponse(savedOrder);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(ORDER_NUMBER_FORMAT);
        long sequence = ORDER_SEQUENCE.incrementAndGet() % 10000;
        return String.format("ORD-%s-%04d", timestamp, sequence);
    }

}
