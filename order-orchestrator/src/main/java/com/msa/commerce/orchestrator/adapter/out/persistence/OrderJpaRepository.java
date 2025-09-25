package com.msa.commerce.orchestrator.adapter.out.persistence;

import com.msa.commerce.orchestrator.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository interface for Order entity operations.
 * Provides database access methods for orders.
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long>,
                                           JpaSpecificationExecutor<OrderJpaEntity> {

    /**
     * Finds an order by its UUID
     */
    Optional<OrderJpaEntity> findByOrderUuid(UUID orderUuid);

    /**
     * Finds an order by its order number
     */
    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);

    /**
     * Checks if order number already exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Finds all orders for a specific user
     */
    List<OrderJpaEntity> findByUserId(Long userId);

    /**
     * Finds all orders for a specific user with specific status
     */
    List<OrderJpaEntity> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Finds orders by status
     */
    List<OrderJpaEntity> findByStatus(OrderStatus status);

    /**
     * Finds orders by status ordered by creation date
     */
    List<OrderJpaEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    /**
     * Finds orders created within date range
     */
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderJpaEntity> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Finds orders for a user created within date range
     */
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderJpaEntity> findUserOrdersByDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Counts orders by status
     */
    long countByStatus(OrderStatus status);

    /**
     * Counts orders for a specific user
     */
    long countByUserId(Long userId);

    /**
     * Finds orders with items (fetch join to avoid N+1 queries)
     */
    @Query("SELECT DISTINCT o FROM OrderJpaEntity o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") Long id);

    /**
     * Finds orders with items by UUID
     */
    @Query("SELECT DISTINCT o FROM OrderJpaEntity o LEFT JOIN FETCH o.orderItems WHERE o.orderUuid = :orderUuid")
    Optional<OrderJpaEntity> findByOrderUuidWithItems(@Param("orderUuid") UUID orderUuid);
}