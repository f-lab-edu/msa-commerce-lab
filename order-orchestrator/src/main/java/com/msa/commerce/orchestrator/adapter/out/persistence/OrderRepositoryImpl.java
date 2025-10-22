package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    private final OrderDomainMapper orderMapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity orderEntity;

        if (order.getOrderId() != null) {
            orderEntity = orderJpaRepository.findByOrderId(order.getOrderId())
                .map(existingEntity -> {
                    existingEntity.updateFrom(order);
                    return existingEntity;
                })
                .orElseGet(() -> OrderJpaEntity.from(order));
        } else {
            orderEntity = OrderJpaEntity.from(order);
        }

        orderEntity.getOrderItems().clear();
        order.getOrderItems().forEach(orderItem -> {
            OrderItemJpaEntity itemEntity = OrderItemJpaEntity.from(orderItem, orderEntity);
            orderEntity.getOrderItems().add(itemEntity);
        });

        OrderJpaEntity savedEntity = orderJpaRepository.save(orderEntity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
            .map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByOrderId(UUID orderId) {
        return orderJpaRepository.findByOrderId(orderId)
            .map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderJpaRepository.findByOrderNumber(orderNumber)
            .map(orderMapper::toDomain);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return orderJpaRepository.existsByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return orderJpaRepository.findByCustomerId(customerId).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        return orderJpaRepository.findByCustomerIdAndStatus(customerId, status).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderJpaRepository.findByStatus(status).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status) {
        return orderJpaRepository.findByStatusOrderByCreatedAtDesc(status).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderJpaRepository.findOrdersByDateRange(startDate, endDate).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public List<Order> findCustomerOrdersByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderJpaRepository.findCustomerOrdersByDateRange(customerId, startDate, endDate).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderJpaRepository.countByStatus(status);
    }

    @Override
    public long countByCustomerId(Long customerId) {
        return orderJpaRepository.countByCustomerId(customerId);
    }

    @Override
    public Optional<Order> findByIdWithItems(Long id) {
        return orderJpaRepository.findByIdWithItems(id)
            .map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByOrderIdWithItems(UUID orderId) {
        return orderJpaRepository.findByOrderIdWithItems(orderId)
            .map(orderMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        orderJpaRepository.deleteById(id);
    }

}
