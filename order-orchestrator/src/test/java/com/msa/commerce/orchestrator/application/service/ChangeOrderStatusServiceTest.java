package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.command.ChangeOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderStatusChangeResult;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeOrderStatusService 테스트")
class ChangeOrderStatusServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    private Order order;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        order = Order.create(
            "ORDER-TEST-001",
            1L,
            createValidShippingAddress(),
            "WEB"
        );
        orderId = order.getOrderId();

        OrderItem orderItem = OrderItem.create(
            1L, "Test Product", "SKU-001", null, null, 1, new BigDecimal("10000.00")
        );
        order.addOrderItem(orderItem);
    }

    @Test
    @DisplayName("주문 상태 변경 성공: PENDING -> CONFIRMED")
    void changeOrderStatus_PendingToConfirmed_Success() {
        // given
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
            orderId,
            OrderStatus.CONFIRMED,
            "Customer confirmed order"
        );

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        OrderStatusChangeResult result = changeOrderStatusService.changeOrderStatus(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.orderNumber()).isEqualTo("ORDER-TEST-001");
        assertThat(result.previousStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.currentStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.changedAt()).isNotNull();

        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("주문 상태 변경 성공: PENDING -> CANCELLED")
    void changeOrderStatus_PendingToCancelled_Success() {
        // given
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
            orderId,
            OrderStatus.CANCELLED,
            "Customer cancelled order"
        );

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        OrderStatusChangeResult result = changeOrderStatusService.changeOrderStatus(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.previousStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.currentStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("주문 상태 변경 성공: CONFIRMED -> PAYMENT_PENDING")
    void changeOrderStatus_ConfirmedToPaymentPending_Success() {
        // given
        order.confirm();
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
            orderId,
            OrderStatus.PAYMENT_PENDING,
            "Payment requested"
        );

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        OrderStatusChangeResult result = changeOrderStatusService.changeOrderStatus(command);

        // then
        assertThat(result.previousStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.currentStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }

    @Test
    @DisplayName("주문 상태 변경 실패: 주문이 존재하지 않음")
    void changeOrderStatus_OrderNotFound_ThrowsException() {
        // given
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
            orderId,
            OrderStatus.CONFIRMED,
            "Customer confirmed order"
        );

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining(orderId.toString());

        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 상태 변경 실패: 잘못된 상태 전이")
    void changeOrderStatus_InvalidTransition_ThrowsException() {
        // given
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
            orderId,
            OrderStatus.PROCESSING,
            "Invalid transition"
        );

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot change order status");

        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 상태 변경 실패: DELIVERED 상태에서 변경 시도")
    void changeOrderStatus_FromDelivered_ThrowsException() {
        // given
        order.confirm();
        order.markPaymentPending();
        order.markPaymentCompleted();
        order.startProcessing();
        order.markShipped();
        order.markDelivered();

        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
            orderId,
            OrderStatus.CANCELLED,
            "Cannot change"
        );

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
            .isInstanceOf(IllegalStateException.class);

        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    private Map<String, Object> createValidShippingAddress() {
        Map<String, Object> address = new HashMap<>();
        address.put("recipient", "홍길동");
        address.put("phone", "010-1234-5678");
        address.put("addressLine1", "서울시 강남구 테헤란로 123");
        address.put("city", "서울시");
        address.put("postalCode", "06234");
        return address;
    }

}
