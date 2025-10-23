package com.msa.commerce.orchestrator.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.OrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderDtoMapper;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.domain.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderDtoMapper orderDtoMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        log.info("Received order creation request for customer: {}", request.customerId());

        CreateOrderCommand command = CreateOrderCommand.builder()
            .customerId(request.customerId())
            .orderItems(request.orderItems().stream()
                .map(item -> CreateOrderCommand.OrderItemCommand.builder()
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .unitPrice(item.unitPrice())
                    .build())
                .toList())
            .build();

        Order order = createOrderUseCase.createOrder(command);
        OrderResponse response = orderDtoMapper.toOrderResponse(order);

        log.info("Order created successfully: orderId={}", order.getOrderId());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
}
