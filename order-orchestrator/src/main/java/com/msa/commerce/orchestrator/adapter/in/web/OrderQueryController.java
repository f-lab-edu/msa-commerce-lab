package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.OrderSearchParams;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.PageResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderSearchParamsMapper;
import com.msa.commerce.orchestrator.application.port.in.GetOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.OrderSearchCriteria;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final GetOrderUseCase getOrderUseCase;

    private final OrderResponseMapper orderResponseMapper;

    private final OrderSearchParamsMapper orderSearchParamsMapper;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        OrderResponse response = getOrderUseCase.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOrders(OrderSearchParams searchParams) {
        searchParams.validateAndSetDefaults();

        OrderSearchCriteria criteria = orderSearchParamsMapper.toCriteria(searchParams);
        Page<OrderSummaryResponse> responsePage = getOrderUseCase.searchOrders(criteria);

        PageResponse<OrderSummaryResponse> pageResponse = orderResponseMapper.toPageResponse(responsePage);

        return ResponseEntity.ok(pageResponse);
    }

}
