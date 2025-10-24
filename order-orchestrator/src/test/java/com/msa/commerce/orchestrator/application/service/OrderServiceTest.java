package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        Long customerId = 1L;
        CreateOrderCommand command = CreateOrderCommand.builder()
            .customerId(customerId)
            .orderItems(List.of(
                CreateOrderCommand.OrderItemCommand.builder()
                    .productId(101L)
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .build()
            ))
            .build();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Order result = orderService.createOrder(command);

        // then
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder).isNotNull();
        assertThat(capturedOrder.getCustomerId()).isEqualTo(customerId);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        assertThat(capturedOrder.getOrderNumber()).isNotNull();
        assertThat(capturedOrder.getOrderNumber()).startsWith("ORD-");

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isNotNull();
    }
}
