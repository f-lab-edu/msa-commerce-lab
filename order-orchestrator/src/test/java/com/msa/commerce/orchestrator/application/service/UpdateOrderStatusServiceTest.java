package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.PublishOrderEventPort;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @InjectMocks
    private UpdateOrderStatusService updateOrderStatusService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Order existingOrder;

    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");

        OrderItem orderItem = OrderItem.builder()
            .orderItemId(UUID.randomUUID())
            .productId(1001L)
            .productName("테스트 상품")
            .productSku("TEST-SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(10000))
            .totalPrice(BigDecimal.valueOf(20000))
            .build();

        existingOrder = Order.builder()
            .orderId(orderId)
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(LocalDateTime.now())
            .shippingAddress(shippingAddress)
            .orderItems(List.of(orderItem))
            .build();
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
            .orderId(orderId)
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        updateOrderStatusService.updateOrderStatus(command);

        verify(orderRepository, times(1)).findByOrderId(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldPublishOrderStatusChangedEventAfterUpdate() {
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
            .orderId(orderId)
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        updateOrderStatusService.updateOrderStatus(command);

        verify(publishOrderEventPort, times(1)).publishOrderStatusChangedEvent(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
            .orderId(orderId)
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateOrderStatusService.updateOrderStatus(command))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining(orderId.toString());

        verify(orderRepository, times(1)).findByOrderId(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(publishOrderEventPort, never()).publishOrderStatusChangedEvent(any(Order.class));
    }

    @Test
    void shouldUpdateOrderStatusCorrectly() {
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
            .orderId(orderId)
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateOrderStatusService.updateOrderStatus(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(capturedOrder.getConfirmedAt()).isNotNull();
    }

    @Test
    void shouldHandleDifferentStatusTransitions() {
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
            .orderId(orderId)
            .newStatus(OrderStatus.CANCELLED)
            .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateOrderStatusService.updateOrderStatus(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(capturedOrder.getCancelledAt()).isNotNull();
    }

}
