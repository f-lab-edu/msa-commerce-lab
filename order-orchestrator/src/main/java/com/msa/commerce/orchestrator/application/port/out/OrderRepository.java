package com.msa.commerce.orchestrator.application.port.out;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderId(UUID orderId);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findCustomerOrdersByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(OrderStatus status);

    long countByCustomerId(Long customerId);

    Optional<Order> findByIdWithItems(Long id);

    Optional<Order> findByOrderIdWithItems(UUID orderId);

    void deleteById(Long id);
}