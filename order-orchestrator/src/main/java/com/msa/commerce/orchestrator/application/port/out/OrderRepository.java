package com.msa.commerce.orchestrator.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderId(UUID orderId);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // 단건 조회 (항목 포함)
    Optional<Order> findByIdWithItems(Long id);

    Optional<Order> findByOrderIdWithItems(UUID orderId);

    void deleteById(Long id);

    // 집계 메서드
    long countByStatus(OrderStatus status);

    long countByCustomerId(Long customerId);

    // 페이징 조회 메서드
    Page<Order> findAll(Pageable pageable);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Page<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
