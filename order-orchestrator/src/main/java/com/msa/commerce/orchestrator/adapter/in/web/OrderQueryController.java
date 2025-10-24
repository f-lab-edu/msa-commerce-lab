package com.msa.commerce.orchestrator.adapter.in.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.OrderSearchParams;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.PageResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.application.port.in.GetOrderUseCase;
import com.msa.commerce.orchestrator.domain.Order;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final GetOrderUseCase getOrderUseCase;

    private final OrderResponseMapper orderResponseMapper;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        Order order = getOrderUseCase.getOrderById(orderId);
        OrderResponse response = orderResponseMapper.toOrderResponse(order);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOrders(OrderSearchParams searchParams) {
        searchParams.validateAndSetDefaults();

        Pageable pageable = createPageable(searchParams);
        Page<Order> orderPage = determineQueryMethod(searchParams, pageable);

        Page<OrderSummaryResponse> responsePage = orderPage.map(orderResponseMapper::toOrderSummaryResponse);
        PageResponse<OrderSummaryResponse> pageResponse = orderResponseMapper.toPageResponse(responsePage);

        return ResponseEntity.ok(pageResponse);
    }

    private Page<Order> determineQueryMethod(OrderSearchParams searchParams, Pageable pageable) {
        Long customerId = searchParams.getCustomerId();
        var status = searchParams.getStatus();
        var startDate = searchParams.getStartDate();
        var endDate = searchParams.getEndDate();

        if (customerId != null && status != null) {
            return getOrderUseCase.getOrdersByCustomerIdAndStatus(customerId, status, pageable);
        }

        if (customerId != null) {
            return getOrderUseCase.getOrdersByCustomerId(customerId, pageable);
        }

        if (status != null) {
            return getOrderUseCase.getOrdersByStatus(status, pageable);
        }

        if (startDate != null && endDate != null) {
            return getOrderUseCase.getOrdersByDateRange(startDate, endDate, pageable);
        }

        return getOrderUseCase.getOrders(pageable);
    }

    private Pageable createPageable(OrderSearchParams searchParams) {
        String sortString = searchParams.getSort();
        String[] sortParts = sortString.split(",");

        Sort sort;
        if (sortParts.length == 2) {
            String property = sortParts[0].trim();
            String direction = sortParts[1].trim();
            sort = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                property
            );
        } else {
            sort = Sort.by(Sort.Direction.DESC, "orderDate");
        }

        return PageRequest.of(searchParams.getPage(), searchParams.getSize(), sort);
    }

}
