package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderService 단위 테스트")
class GetOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderResponseMapper orderResponseMapper;

    @InjectMocks
    private GetOrderService getOrderService;

    @Test
    @DisplayName("주문 ID로 주문 조회 성공")
    void getOrderById_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = createTestOrder(orderId, 1001L, OrderStatus.DELIVERED);
        OrderResponse orderResponse = createTestOrderResponse(orderId, 1001L, OrderStatus.DELIVERED);

        when(orderRepository.findByOrderIdWithItems(orderId))
            .thenReturn(Optional.of(order));
        when(orderResponseMapper.toOrderResponse(order))
            .thenReturn(orderResponse);

        // When
        OrderResponse result = getOrderService.getOrderById(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getCustomerId()).isEqualTo(1001L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).findByOrderIdWithItems(orderId);
        verify(orderResponseMapper).toOrderResponse(order);
    }

    @Test
    @DisplayName("주문 ID로 주문 조회 시 주문이 없으면 OrderNotFoundException 발생")
    void getOrderById_OrderNotFound() {
        // Given
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findByOrderIdWithItems(orderId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrderById(orderId))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("주문을 찾을 수 없습니다")
            .hasMessageContaining(orderId.toString());

        verify(orderRepository).findByOrderIdWithItems(orderId);
        verifyNoInteractions(orderResponseMapper);
    }

    @Test
    @DisplayName("전체 주문 목록 조회 성공")
    void getOrders_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(
            createTestOrder(UUID.randomUUID(), 1001L, OrderStatus.DELIVERED),
            createTestOrder(UUID.randomUUID(), 1002L, OrderStatus.SHIPPED)
        );
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderResponseMapper.toOrderSummaryResponse(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return createTestOrderSummary(order.getOrderId(), order.getCustomerId(), order.getStatus());
            });

        // When
        Page<OrderSummaryResponse> result = getOrderService.getOrders(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(orderRepository).findAll(pageable);
        verify(orderResponseMapper, times(2)).toOrderSummaryResponse(any(Order.class));
    }

    @Test
    @DisplayName("고객 ID로 주문 목록 조회 성공")
    void getOrdersByCustomerId_Success() {
        // Given
        Long customerId = 1001L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(
            createTestOrder(UUID.randomUUID(), customerId, OrderStatus.DELIVERED),
            createTestOrder(UUID.randomUUID(), customerId, OrderStatus.SHIPPED)
        );
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByCustomerId(customerId, pageable)).thenReturn(orderPage);
        when(orderResponseMapper.toOrderSummaryResponse(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return createTestOrderSummary(order.getOrderId(), order.getCustomerId(), order.getStatus());
            });

        // When
        Page<OrderSummaryResponse> result = getOrderService.getOrdersByCustomerId(customerId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .allMatch(summary -> summary.getCustomerId().equals(customerId));
        verify(orderRepository).findByCustomerId(customerId, pageable);
        verify(orderResponseMapper, times(2)).toOrderSummaryResponse(any(Order.class));
    }

    @Test
    @DisplayName("고객 ID가 null이면 IllegalArgumentException 발생")
    void getOrdersByCustomerId_NullCustomerId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByCustomerId(null, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("고객 ID는 필수입니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    @Test
    @DisplayName("주문 상태로 주문 목록 조회 성공")
    void getOrdersByStatus_Success() {
        // Given
        OrderStatus status = OrderStatus.DELIVERED;
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(
            createTestOrder(UUID.randomUUID(), 1001L, status),
            createTestOrder(UUID.randomUUID(), 1002L, status)
        );
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByStatus(status, pageable)).thenReturn(orderPage);
        when(orderResponseMapper.toOrderSummaryResponse(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return createTestOrderSummary(order.getOrderId(), order.getCustomerId(), order.getStatus());
            });

        // When
        Page<OrderSummaryResponse> result = getOrderService.getOrdersByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .allMatch(summary -> summary.getStatus().equals(status));
        verify(orderRepository).findByStatus(status, pageable);
        verify(orderResponseMapper, times(2)).toOrderSummaryResponse(any(Order.class));
    }

    @Test
    @DisplayName("주문 상태가 null이면 IllegalArgumentException 발생")
    void getOrdersByStatus_NullStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByStatus(null, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 상태는 필수입니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    @Test
    @DisplayName("고객 ID와 주문 상태로 주문 목록 조회 성공")
    void getOrdersByCustomerIdAndStatus_Success() {
        // Given
        Long customerId = 1001L;
        OrderStatus status = OrderStatus.DELIVERED;
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(
            createTestOrder(UUID.randomUUID(), customerId, status)
        );
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByCustomerIdAndStatus(customerId, status, pageable))
            .thenReturn(orderPage);
        when(orderResponseMapper.toOrderSummaryResponse(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return createTestOrderSummary(order.getOrderId(), order.getCustomerId(), order.getStatus());
            });

        // When
        Page<OrderSummaryResponse> result = getOrderService.getOrdersByCustomerIdAndStatus(customerId, status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(customerId);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(status);
        verify(orderRepository).findByCustomerIdAndStatus(customerId, status, pageable);
        verify(orderResponseMapper).toOrderSummaryResponse(any(Order.class));
    }

    @Test
    @DisplayName("고객 ID와 주문 상태 조회 시 고객 ID가 null이면 IllegalArgumentException 발생")
    void getOrdersByCustomerIdAndStatus_NullCustomerId() {
        // Given
        OrderStatus status = OrderStatus.DELIVERED;
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByCustomerIdAndStatus(null, status, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("고객 ID는 필수입니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    @Test
    @DisplayName("고객 ID와 주문 상태 조회 시 주문 상태가 null이면 IllegalArgumentException 발생")
    void getOrdersByCustomerIdAndStatus_NullStatus() {
        // Given
        Long customerId = 1001L;
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByCustomerIdAndStatus(customerId, null, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 상태는 필수입니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    @Test
    @DisplayName("날짜 범위로 주문 목록 조회 성공")
    void getOrdersByDateRange_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 10, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(
            createTestOrder(UUID.randomUUID(), 1001L, OrderStatus.DELIVERED)
        );
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findOrdersByDateRange(startDate, endDate, pageable))
            .thenReturn(orderPage);
        when(orderResponseMapper.toOrderSummaryResponse(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return createTestOrderSummary(order.getOrderId(), order.getCustomerId(), order.getStatus());
            });

        // When
        Page<OrderSummaryResponse> result = getOrderService.getOrdersByDateRange(startDate, endDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findOrdersByDateRange(startDate, endDate, pageable);
        verify(orderResponseMapper).toOrderSummaryResponse(any(Order.class));
    }

    @Test
    @DisplayName("날짜 범위 조회 시 시작 날짜가 null이면 IllegalArgumentException 발생")
    void getOrdersByDateRange_NullStartDate() {
        // Given
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByDateRange(null, endDate, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시작 일시와 종료 일시는 필수입니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    @Test
    @DisplayName("날짜 범위 조회 시 종료 날짜가 null이면 IllegalArgumentException 발생")
    void getOrdersByDateRange_NullEndDate() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 10, 1, 0, 0);
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByDateRange(startDate, null, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시작 일시와 종료 일시는 필수입니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    @Test
    @DisplayName("날짜 범위 조회 시 시작 날짜가 종료 날짜보다 이후이면 IllegalArgumentException 발생")
    void getOrdersByDateRange_InvalidDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 10, 31, 23, 59);
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 1, 0, 0);
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrdersByDateRange(startDate, endDate, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시작 일시는 종료 일시보다 이전이어야 합니다.");

        verifyNoInteractions(orderRepository, orderResponseMapper);
    }

    // Helper method to create test Order
    private Order createTestOrder(UUID orderId, Long customerId, OrderStatus status) {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipientName", "김철수");
        shippingAddress.put("phone", "010-1234-5678");
        shippingAddress.put("zipCode", "06234");
        shippingAddress.put("address", "서울특별시 강남구 테헤란로 123");

        return Order.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251025-0001")
            .customerId(customerId)
            .status(status)
            .subtotalAmount(new BigDecimal("100000"))
            .taxAmount(new BigDecimal("10000"))
            .shippingAmount(new BigDecimal("3000"))
            .discountAmount(new BigDecimal("5000"))
            .totalAmount(new BigDecimal("108000"))
            .currency("KRW")
            .shippingAddress(shippingAddress)
            .orderDate(LocalDateTime.of(2025, 10, 20, 10, 30))
            .sourceChannel("WEB")
            .version(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .orderItems(List.of())
            .build();
    }

    // Helper method to create test OrderResponse
    private OrderResponse createTestOrderResponse(UUID orderId, Long customerId, OrderStatus status) {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipientName", "김철수");
        shippingAddress.put("phone", "010-1234-5678");
        shippingAddress.put("zipCode", "06234");
        shippingAddress.put("address", "서울특별시 강남구 테헤란로 123");

        return OrderResponse.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251025-0001")
            .customerId(customerId)
            .status(status)
            .subtotalAmount(new BigDecimal("100000"))
            .taxAmount(new BigDecimal("10000"))
            .shippingAmount(new BigDecimal("3000"))
            .discountAmount(new BigDecimal("5000"))
            .totalAmount(new BigDecimal("108000"))
            .currency("KRW")
            .shippingAddress(shippingAddress)
            .orderDate(LocalDateTime.of(2025, 10, 20, 10, 30))
            .sourceChannel("WEB")
            .totalItemCount(0)
            .orderItems(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // Helper method to create test OrderSummaryResponse
    private OrderSummaryResponse createTestOrderSummary(UUID orderId, Long customerId, OrderStatus status) {
        return OrderSummaryResponse.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251025-0001")
            .customerId(customerId)
            .status(status)
            .totalAmount(new BigDecimal("108000"))
            .currency("KRW")
            .totalItemCount(0)
            .orderDate(LocalDateTime.of(2025, 10, 20, 10, 30))
            .sourceChannel("WEB")
            .build();
    }

}
