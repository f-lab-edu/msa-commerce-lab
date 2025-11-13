package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.OrderStatusChangeRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderStatusChangeResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderStatusChangeMapper;
import com.msa.commerce.orchestrator.application.port.in.ChangeOrderStatusUseCase;
import com.msa.commerce.orchestrator.application.port.in.command.ChangeOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderStatusChangeResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderCommandController {

    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;

    private final OrderStatusChangeMapper orderStatusChangeMapper;

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusChangeResponse> changeOrderStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody OrderStatusChangeRequest request
    ) {
        ChangeOrderStatusCommand command = orderStatusChangeMapper.toCommand(orderId, request);
        OrderStatusChangeResult result = changeOrderStatusUseCase.changeOrderStatus(command);
        OrderStatusChangeResponse response = orderStatusChangeMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

}
