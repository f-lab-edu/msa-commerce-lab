package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long>, JpaSpecificationExecutor<OrderJpaEntity> {

    Optional<OrderJpaEntity> findByOrderUuid(UUID orderUuid);

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<OrderJpaEntity> findByUserId(Long userId);

    List<OrderJpaEntity> findByUserIdAndStatus(Long userId, OrderStatus status);

    List<OrderJpaEntity> findByStatus(OrderStatus status);

    List<OrderJpaEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderJpaEntity> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderJpaEntity> findUserOrdersByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByStatus(OrderStatus status);

    long countByUserId(Long userId);

    @Query("SELECT DISTINCT o FROM OrderJpaEntity o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM OrderJpaEntity o LEFT JOIN FETCH o.orderItems WHERE o.orderUuid = :orderUuid")
    Optional<OrderJpaEntity> findByOrderUuidWithItems(@Param("orderUuid") UUID orderUuid);

}
