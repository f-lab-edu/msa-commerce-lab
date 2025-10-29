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

        // JPA 영속성 컨텍스트의 변경 감지(dirty checking)를 활용한 OrderItems 동기화
        // 1. 기존 컬렉션 clear(): 삭제된 항목을 orphanRemoval로 자동 처리
        // 2. 도메인 모델의 orderItems를 순회하며 새로운 엔티티 생성 후 추가
        // 3. JPA가 컬렉션 변경사항을 추적하여 INSERT/UPDATE/DELETE 자동 수행
        orderEntity.getOrderItems().clear();
        order.getOrderItems().forEach(orderItem -> {
            OrderItemJpaEntity itemEntity = OrderItemJpaEntity.from(orderItem, orderEntity);
            orderEntity.getOrderItems().add(itemEntity);
        });

        OrderJpaEntity savedEntity = orderJpaRepository.save(orderEntity);
        return orderMapper.toDomain(savedEntity);
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
        return orderJpaRepository.findByOrderDateBetween(startDate, endDate).stream()
            .map(orderMapper::toDomain)
            .toList();
    }

    @Override
    public List<Order> findCustomerOrdersByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderJpaRepository.findByCustomerIdAndOrderDateBetween(customerId, startDate, endDate).stream()
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
    public Optional<Order> findByOrderIdWithItems(UUID orderId) {
        return orderJpaRepository.findWithItemsByOrderId(orderId)
            .map(orderMapper::toDomain);
    }

    @Override
    public void deleteByOrderId(UUID orderId) {
        orderJpaRepository.deleteById(orderId);
    }

}
