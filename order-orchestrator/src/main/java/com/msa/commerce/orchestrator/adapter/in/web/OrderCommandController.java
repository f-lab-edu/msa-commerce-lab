package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CancelOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderCommandMapper;
import com.msa.commerce.orchestrator.application.port.in.CancelOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.ConfirmOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.PayOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.command.CancelOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.command.ConfirmOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.command.PayOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderCommandController {

    private final ConfirmOrderUseCase confirmOrderUseCase;

    private final PayOrderUseCase payOrderUseCase;

    private final CancelOrderUseCase cancelOrderUseCase;

    private final OrderCommandMapper orderCommandMapper;

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable UUID orderId) {
        ConfirmOrderCommand command = orderCommandMapper.toConfirmCommand(orderId);
        OrderResponse response = confirmOrderUseCase.confirm(command);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/paid")
    public ResponseEntity<OrderResponse> markOrderPaid(@PathVariable UUID orderId) {
        PayOrderCommand command = orderCommandMapper.toPayCommand(orderId);
        OrderResponse response = payOrderUseCase.pay(command);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId, @Valid @RequestBody CancelOrderRequest request) {
        CancelOrderCommand command = orderCommandMapper.toCancelCommand(orderId, request);
        OrderResponse response = cancelOrderUseCase.cancel(command);

        return ResponseEntity.ok(response);
    }

}
