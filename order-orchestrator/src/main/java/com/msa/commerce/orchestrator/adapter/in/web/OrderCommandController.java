package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CancelOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.CreateOrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderCommandMapper;
import com.msa.commerce.orchestrator.application.port.in.CancelOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.ConfirmOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.PayOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderCommandController {

    private final CreateOrderUseCase createOrderUseCase;

    private final ConfirmOrderUseCase confirmOrderUseCase;

    private final PayOrderUseCase payOrderUseCase;

    private final CancelOrderUseCase cancelOrderUseCase;

    private final OrderCommandMapper orderCommandMapper;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CreateOrderResponse.builder()
                .orderId(createOrderUseCase.createOrder(orderCommandMapper.toCreateOrderCommand(request)))
                .message("Order created successfully")
                .build());
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(confirmOrderUseCase.confirm(orderCommandMapper.toConfirmCommand(orderId)));
    }

    @PostMapping("/{orderId}/paid")
    public ResponseEntity<OrderResponse> markOrderPaid(@PathVariable UUID orderId) {
        return ResponseEntity.ok(payOrderUseCase.paid(orderCommandMapper.toPayCommand(orderId)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId, @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(cancelOrderUseCase.cancel(orderCommandMapper.toCancelCommand(orderId, request)));
    }

}
