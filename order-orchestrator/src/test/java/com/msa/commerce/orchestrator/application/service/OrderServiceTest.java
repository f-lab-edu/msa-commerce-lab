package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.OrderResponse;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.ProductPort;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapperImpl;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.vo.ProductInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductPort productPort;

    private OrderResponseMapper orderResponseMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderResponseMapper = new OrderResponseMapperImpl();
        orderService = new OrderService(orderRepository, productPort, orderResponseMapper);
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        Long customerId = 1L;
        Long productId = 101L;
        CreateOrderCommand command = CreateOrderCommand.builder()
            .customerId(customerId)
            .orderItems(List.of(
                CreateOrderCommand.OrderItemCommand.builder()
                    .productId(productId)
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .build()
            ))
            .build();

        ProductInfo productInfo = new ProductInfo(
            productId,
            "테스트 상품",
            "TEST-SKU-001",
            "https://example.com/image.jpg",
            "상품 설명",
            BigDecimal.valueOf(10000)
        );

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(productPort.getProductInfo(productId)).thenReturn(productInfo);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderResponse result = orderService.createOrder(command);

        // then
        verify(productPort, times(1)).getProductInfo(productId);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder).isNotNull();
        assertThat(capturedOrder.getCustomerId()).isEqualTo(customerId);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        assertThat(capturedOrder.getOrderNumber()).isNotNull();
        assertThat(capturedOrder.getOrderNumber()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

        assertThat(result).isNotNull();
        assertThat(result.orderId()).isNotNull();
        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
    }

}
