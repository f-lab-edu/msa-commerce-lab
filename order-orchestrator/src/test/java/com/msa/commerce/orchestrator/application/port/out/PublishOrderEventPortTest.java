package com.msa.commerce.orchestrator.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

class PublishOrderEventPortTest {

    @Test
    void shouldDefinePublishOrderCreatedEventMethod() {
        PublishOrderEventPort port = mock(PublishOrderEventPort.class);

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");

        Order order = Order.builder()
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(LocalDateTime.now())
            .shippingAddress(shippingAddress)
            .orderItems(List.of())
            .build();

        doNothing().when(port).publishOrderCreatedEvent(any(Order.class));

        port.publishOrderCreatedEvent(order);

        verify(port, times(1)).publishOrderCreatedEvent(order);
    }

    @Test
    void shouldDefinePublishOrderStatusChangedEventMethod() {
        PublishOrderEventPort port = mock(PublishOrderEventPort.class);

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");

        Order order = Order.builder()
            .orderNumber("ORD-20240115-002")
            .customerId(12345L)
            .status(OrderStatus.CONFIRMED)
            .totalAmount(BigDecimal.valueOf(30000))
            .orderDate(LocalDateTime.now())
            .shippingAddress(shippingAddress)
            .orderItems(List.of())
            .build();

        doNothing().when(port).publishOrderStatusChangedEvent(any(Order.class));

        port.publishOrderStatusChangedEvent(order);

        verify(port, times(1)).publishOrderStatusChangedEvent(order);
    }

    @Test
    void shouldBeAnInterface() {
        assertThat(PublishOrderEventPort.class.isInterface()).isTrue();
    }

    @Test
    void shouldHaveTwoMethods() {
        assertThat(PublishOrderEventPort.class.getDeclaredMethods()).hasSize(2);
    }
}
