package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.PublishOrderEventPort;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private CreateOrderCommand command;

    @BeforeEach
    void setUp() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");
        shippingAddress.put("street", "강남대로 123");

        CreateOrderCommand.OrderItemCommand orderItem = CreateOrderCommand.OrderItemCommand.builder()
            .productId(1001L)
            .productName("테스트 상품")
            .productSku("TEST-SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(10000))
            .build();

        command = CreateOrderCommand.builder()
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .shippingAddress(shippingAddress)
            .sourceChannel("WEB")
            .orderItems(List.of(orderItem))
            .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        Order savedOrder = Order.builder()
            .orderId(UUID.randomUUID())
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .status(OrderStatus.PENDING)
            .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        UUID orderId = createOrderService.createOrder(command);

        assertThat(orderId).isNotNull();
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldPublishOrderCreatedEventAfterSaving() {
        Order savedOrder = Order.builder()
            .orderId(UUID.randomUUID())
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .status(OrderStatus.PENDING)
            .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        createOrderService.createOrder(command);

        verify(publishOrderEventPort, times(1)).publishOrderCreatedEvent(any(Order.class));
    }

    @Test
    void shouldCreateOrderWithCorrectData() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return Order.builder()
                .orderId(UUID.randomUUID())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .shippingAddress(order.getShippingAddress())
                .orderItems(order.getOrderItems())
                .build();
        });

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getOrderNumber()).isEqualTo("ORD-20240115-001");
        assertThat(capturedOrder.getCustomerId()).isEqualTo(12345L);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    void shouldCreateOrderItemsCorrectly() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return Order.builder()
                .orderId(UUID.randomUUID())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .orderItems(order.getOrderItems())
                .build();
        });

        createOrderService.createOrder(command);

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        assertThat(capturedOrder.getOrderItems().get(0).getProductId()).isEqualTo(1001L);
        assertThat(capturedOrder.getOrderItems().get(0).getProductName()).isEqualTo("테스트 상품");
        assertThat(capturedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(capturedOrder.getOrderItems().get(0).getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

}
