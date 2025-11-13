package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.UpdateOrderStatusRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.CreateOrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.UpdateOrderStatusResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderCommandMapper;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderCommandController {

    private final CreateOrderUseCase createOrderUseCase;

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    private final OrderCommandMapper orderCommandMapper;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = orderCommandMapper.toCreateOrderCommand(request);
        UUID orderId = createOrderUseCase.createOrder(command);

        CreateOrderResponse response = CreateOrderResponse.builder()
            .orderId(orderId)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<UpdateOrderStatusResponse> updateOrderStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody UpdateOrderStatusRequest request) {

        UpdateOrderStatusCommand command = orderCommandMapper.toUpdateOrderStatusCommand(orderId, request);
        updateOrderStatusUseCase.updateOrderStatus(command);

        UpdateOrderStatusResponse response = UpdateOrderStatusResponse.builder()
            .message("주문 상태가 성공적으로 변경되었습니다.")
            .build();

        return ResponseEntity.ok(response);
    }

}
