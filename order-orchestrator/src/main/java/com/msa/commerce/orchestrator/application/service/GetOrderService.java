package com.msa.commerce.orchestrator.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.GetOrderUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
    public Order getOrderById(UUID orderId) {
        return orderRepository.findByOrderIdWithItems(orderId)
            .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다. orderId: " + orderId));
    }

    @Override
    public Page<Order> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        if (customerId == null) {
            throw new IllegalArgumentException("고객 ID는 필수입니다.");
        }
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 필수입니다.");
        }
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Order> getOrdersByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable) {
        if (customerId == null) {
            throw new IllegalArgumentException("고객 ID는 필수입니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 필수입니다.");
        }
        return orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
    }

    @Override
    public Page<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작 일시와 종료 일시는 필수입니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 일시는 종료 일시보다 이전이어야 합니다.");
        }
        return orderRepository.findOrdersByDateRange(startDate, endDate, pageable);
    }

}
