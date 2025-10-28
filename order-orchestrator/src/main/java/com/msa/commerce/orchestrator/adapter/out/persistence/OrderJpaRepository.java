package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long>, JpaSpecificationExecutor<OrderJpaEntity> {

    Optional<OrderJpaEntity> findByOrderId(UUID orderId);

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<OrderJpaEntity> findByCustomerId(Long customerId);

    List<OrderJpaEntity> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    List<OrderJpaEntity> findByStatus(OrderStatus status);

    List<OrderJpaEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<OrderJpaEntity> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<OrderJpaEntity> findByCustomerIdAndOrderDateBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(OrderStatus status);

    long countByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<OrderJpaEntity> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<OrderJpaEntity> findWithItemsByOrderId(UUID orderId);

}
