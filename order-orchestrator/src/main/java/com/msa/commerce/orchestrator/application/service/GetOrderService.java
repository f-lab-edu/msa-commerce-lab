package com.msa.commerce.orchestrator.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.GetOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.OrderSearchCriteria;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    private final OrderResponseMapper orderResponseMapper;

    @Override
    @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findByOrderIdWithItems(orderId)
            .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다. orderId: " + orderId));
        return orderResponseMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderSummaryResponse> searchOrders(OrderSearchCriteria criteria) {
        Pageable pageable = createPageable(criteria);
        return determineQueryMethod(criteria, pageable);
    }

    private Pageable createPageable(OrderSearchCriteria criteria) {
        String sortString = criteria.getSort();

        // Null safety check to prevent NullPointerException
        if (sortString == null || sortString.trim().isEmpty()) {
            return PageRequest.of(criteria.getPage(), criteria.getSize(),
                Sort.by(Sort.Direction.DESC, "orderDate"));
        }

        String[] sortParts = sortString.split(",");

        Sort sort;
        if (sortParts.length == 2) {
            String property = sortParts[0].trim();
            String direction = sortParts[1].trim();
            sort = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                property
            );
        } else {
            sort = Sort.by(Sort.Direction.DESC, "orderDate");
        }

        return PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
    }

    private Page<OrderSummaryResponse> determineQueryMethod(OrderSearchCriteria criteria, Pageable pageable) {
        Long customerId = criteria.getCustomerId();
        OrderStatus status = criteria.getStatus();
        LocalDateTime startDate = criteria.getStartDate();
        LocalDateTime endDate = criteria.getEndDate();

        if (customerId != null && status != null) {
            return getOrdersByCustomerIdAndStatus(customerId, status, pageable);
        }

        if (customerId != null) {
            return getOrdersByCustomerId(customerId, pageable);
        }

        if (status != null) {
            return getOrdersByStatus(status, pageable);
        }

        if (startDate != null && endDate != null) {
            return getOrdersByDateRange(startDate, endDate, pageable);
        }

        return getOrders(pageable);
    }

    @Override
    public Page<OrderSummaryResponse> getOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(orderResponseMapper::toOrderSummaryResponse);
    }

    @Override
    public Page<OrderSummaryResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        if (customerId == null) {
            throw new IllegalArgumentException("고객 ID는 필수입니다.");
        }
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        return orderPage.map(orderResponseMapper::toOrderSummaryResponse);
    }

    @Override
    public Page<OrderSummaryResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 필수입니다.");
        }
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);
        return orderPage.map(orderResponseMapper::toOrderSummaryResponse);
    }

    @Override
    public Page<OrderSummaryResponse> getOrdersByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable) {
        if (customerId == null) {
            throw new IllegalArgumentException("고객 ID는 필수입니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 필수입니다.");
        }
        Page<Order> orderPage = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        return orderPage.map(orderResponseMapper::toOrderSummaryResponse);
    }

    @Override
    public Page<OrderSummaryResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작 일시와 종료 일시는 필수입니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 일시는 종료 일시보다 이전이어야 합니다.");
        }
        Page<Order> orderPage = orderRepository.findOrdersByDateRange(startDate, endDate, pageable);
        return orderPage.map(orderResponseMapper::toOrderSummaryResponse);
    }

}
