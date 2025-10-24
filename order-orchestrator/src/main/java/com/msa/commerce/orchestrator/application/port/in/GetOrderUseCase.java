package com.msa.commerce.orchestrator.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface GetOrderUseCase {

    Order getOrderById(UUID orderId);

    Page<Order> getOrders(Pageable pageable);

    Page<Order> getOrdersByCustomerId(Long customerId, Pageable pageable);

    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);

    Page<Order> getOrdersByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Page<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
