package com.msa.commerce.orchestrator.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface GetOrderUseCase {

    OrderResponse getOrderById(UUID orderId);

    Page<OrderSummaryResponse> searchOrders(OrderSearchCriteria criteria);

    Page<OrderSummaryResponse> getOrders(Pageable pageable);

    Page<OrderSummaryResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);

    Page<OrderSummaryResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

    Page<OrderSummaryResponse> getOrdersByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Page<OrderSummaryResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
