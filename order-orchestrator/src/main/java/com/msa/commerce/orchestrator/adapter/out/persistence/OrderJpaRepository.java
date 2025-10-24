package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long>, JpaSpecificationExecutor<OrderJpaEntity> {

    Optional<OrderJpaEntity> findByOrderId(UUID orderId);

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT DISTINCT o FROM OrderJpaEntity o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM OrderJpaEntity o LEFT JOIN FETCH o.orderItems WHERE o.orderId = :orderId")
    Optional<OrderJpaEntity> findByOrderIdWithItems(@Param("orderId") UUID orderId);

    long countByStatus(OrderStatus status);

    long countByCustomerId(Long customerId);

    // 페이징 조회 메서드
    Page<OrderJpaEntity> findByCustomerId(Long customerId, Pageable pageable);

    Page<OrderJpaEntity> findByStatus(OrderStatus status, Pageable pageable);

    Page<OrderJpaEntity> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Page<OrderJpaEntity> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

}
