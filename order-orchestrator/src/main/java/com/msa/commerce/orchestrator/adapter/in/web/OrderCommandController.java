package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.UpdateOrderStatusRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.CreateOrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.CreateOrderRequestMapper;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCommandController {

    private final CreateOrderUseCase createOrderUseCase;

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    private final CreateOrderRequestMapper createOrderRequestMapper;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        UUID orderId = createOrderUseCase.createOrder(
            createOrderRequestMapper.toCommand(request)
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CreateOrderResponse.builder()
                .orderId(orderId)
                .message("Order created successfully")
                .build());
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        updateOrderStatusUseCase.updateOrderStatus(
            orderId,
            request.getNewStatus(),
            request.getReason()
        );

        return ResponseEntity.noContent().build();
    }

}
