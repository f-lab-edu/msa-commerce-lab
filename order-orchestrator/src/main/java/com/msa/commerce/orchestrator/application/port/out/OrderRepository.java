package com.msa.commerce.orchestrator.application.port.out;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port interface for Order domain operations.
 * Defines the contract for order persistence operations.
 */
public interface OrderRepository {

    /**
     * Saves an order
     */
    Order save(Order order);

    /**
     * Finds an order by its ID
     */
    Optional<Order> findById(Long id);

    /**
     * Finds an order by its UUID
     */
    Optional<Order> findByOrderId(UUID orderId);

    /**
     * Finds an order by its order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Checks if order number already exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Finds all orders for a specific customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Finds all orders for a specific customer with specific status
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    /**
     * Finds orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders by status ordered by creation date
     */
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    /**
     * Finds orders created within date range
     */
    List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds orders for a customer created within date range
     */
    List<Order> findCustomerOrdersByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Counts orders by status
     */
    long countByStatus(OrderStatus status);

    /**
     * Counts orders for a specific customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Finds orders with their items (for performance optimization)
     */
    Optional<Order> findByIdWithItems(Long id);

    /**
     * Finds orders with their items by UUID
     */
    Optional<Order> findByOrderIdWithItems(UUID orderId);

    /**
     * Deletes an order by its ID (if business rules allow)
     */
    void deleteById(Long id);
}