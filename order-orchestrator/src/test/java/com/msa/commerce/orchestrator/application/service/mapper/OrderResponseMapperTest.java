package com.msa.commerce.orchestrator.application.service.mapper;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.msa.commerce.orchestrator.application.port.in.response.OrderItemResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.domain.AddressType;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.vo.ShippingAddress;

@DisplayName("OrderResponseMapper 테스트")
class OrderResponseMapperTest {

    private OrderResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderResponseMapperImpl();
    }

    @Test
    @DisplayName("Order를 OrderResponse로 변환 성공")
    void toOrderResponse_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        Long customerId = 1001L;
        LocalDateTime orderDate = LocalDateTime.of(2025, 10, 20, 10, 30);

        ShippingAddress shippingAddress = ShippingAddress.create(
            AddressType.DEFAULT,
            "홍길동",
            "010-1234-5678",
            "06234",
            "서울특별시 강남구 테헤란로 123",
            null
        );

        OrderItem item1 = OrderItem.builder()
            .orderItemId(UUID.randomUUID())
            .productId(100L)
            .productName("상품1")
            .quantity(2)
            .unitPrice(new BigDecimal("50000"))
            .totalPrice(new BigDecimal("100000"))
            .build();

        OrderItem item2 = OrderItem.builder()
            .orderItemId(UUID.randomUUID())
            .productId(200L)
            .productName("상품2")
            .quantity(1)
            .unitPrice(new BigDecimal("30000"))
            .totalPrice(new BigDecimal("30000"))
            .build();

        Order order = Order.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251020-0001")
            .customerId(customerId)
            .status(OrderStatus.PENDING)
            .subtotalAmount(new BigDecimal("130000"))
            .taxAmount(new BigDecimal("13000"))
            .shippingAmount(new BigDecimal("3000"))
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(new BigDecimal("146000"))
            .currency("KRW")
            .shippingAddress(shippingAddress)
            .orderDate(orderDate)
            .sourceChannel("WEB")
            .orderItems(List.of(item1, item2))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        OrderResponse response = mapper.toOrderResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-20251020-0001");
        assertThat(response.getCustomerId()).isEqualTo(customerId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getSubtotalAmount()).isEqualByComparingTo(new BigDecimal("130000"));
        assertThat(response.getTaxAmount()).isEqualByComparingTo(new BigDecimal("13000"));
        assertThat(response.getShippingAmount()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("146000"));
        assertThat(response.getCurrency()).isEqualTo("KRW");
        assertThat(response.getShippingAddress()).isNotNull();
        assertThat(response.getOrderDate()).isEqualTo(orderDate);
        assertThat(response.getSourceChannel()).isEqualTo("WEB");
        assertThat(response.getTotalItemCount()).isEqualTo(3); // 2 + 1 quantities
        assertThat(response.getOrderItems()).hasSize(2);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Order를 OrderResponse로 변환 - 주문 아이템이 없는 경우")
    void toOrderResponse_NoItems_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251020-0002")
            .customerId(1002L)
            .status(OrderStatus.CANCELLED)
            .subtotalAmount(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO)
            .shippingAmount(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .currency("KRW")
            .shippingAddress(ShippingAddress.createDefault())
            .orderDate(LocalDateTime.now())
            .sourceChannel("MOBILE")
            .orderItems(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        OrderResponse response = mapper.toOrderResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getTotalItemCount()).isEqualTo(0);
        assertThat(response.getOrderItems()).isEmpty();
    }

    @Test
    @DisplayName("OrderItem을 OrderItemResponse로 변환 성공")
    void toOrderItemResponse_Success() {
        // Given
        UUID orderItemId = UUID.randomUUID();
        OrderItem orderItem = OrderItem.builder()
            .orderItemId(orderItemId)
            .productId(500L)
            .productName("테스트 상품")
            .quantity(3)
            .unitPrice(new BigDecimal("25000"))
            .totalPrice(new BigDecimal("75000"))
            .build();

        // When
        OrderItemResponse response = mapper.toOrderItemResponse(orderItem);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderItemId()).isEqualTo(orderItemId);
        assertThat(response.getProductId()).isEqualTo(500L);
        assertThat(response.getProductName()).isEqualTo("테스트 상품");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getUnitPrice()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("75000"));
    }

    @Test
    @DisplayName("Order를 OrderSummaryResponse로 변환 성공")
    void toOrderSummaryResponse_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        LocalDateTime orderDate = LocalDateTime.of(2025, 10, 21, 15, 45);

        Order order = Order.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251021-0001")
            .customerId(2001L)
            .status(OrderStatus.DELIVERED)
            .totalAmount(new BigDecimal("250000"))
            .currency("KRW")
            .orderDate(orderDate)
            .sourceChannel("API")
            .orderItems(List.of(
                OrderItem.builder()
                    .orderItemId(UUID.randomUUID())
                    .productId(1L)
                    .productName("Product 1")
                    .quantity(2)
                    .unitPrice(new BigDecimal("50000"))
                    .totalPrice(new BigDecimal("100000"))
                    .build(),
                OrderItem.builder()
                    .orderItemId(UUID.randomUUID())
                    .productId(2L)
                    .productName("Product 2")
                    .quantity(1)
                    .unitPrice(new BigDecimal("150000"))
                    .totalPrice(new BigDecimal("150000"))
                    .build()
            ))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        OrderSummaryResponse response = mapper.toOrderSummaryResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-20251021-0001");
        assertThat(response.getCustomerId()).isEqualTo(2001L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("250000"));
        assertThat(response.getCurrency()).isEqualTo("KRW");
        assertThat(response.getTotalItemCount()).isEqualTo(3);
        assertThat(response.getOrderDate()).isEqualTo(orderDate);
        assertThat(response.getSourceChannel()).isEqualTo("API");
    }

    @Test
    @DisplayName("Page<Order>를 PageResponse<OrderSummaryResponse>로 변환 성공")
    void toPageResponse_Success() {
        // Given
        Order order1 = Order.builder()
            .orderId(UUID.randomUUID())
            .orderNumber("ORD-001")
            .customerId(3001L)
            .status(OrderStatus.PENDING)
            .totalAmount(new BigDecimal("100000"))
            .currency("KRW")
            .orderDate(LocalDateTime.now())
            .sourceChannel("WEB")
            .orderItems(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Order order2 = Order.builder()
            .orderId(UUID.randomUUID())
            .orderNumber("ORD-002")
            .customerId(3002L)
            .status(OrderStatus.SHIPPED)
            .totalAmount(new BigDecimal("200000"))
            .currency("KRW")
            .orderDate(LocalDateTime.now())
            .sourceChannel("MOBILE")
            .orderItems(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Page<OrderSummaryResponse> page = new PageImpl<>(
            List.of(
                mapper.toOrderSummaryResponse(order1),
                mapper.toOrderSummaryResponse(order2)
            ),
            PageRequest.of(0, 10),
            2
        );

        // When
        var response = mapper.toPageResponse(page);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("빈 Page를 PageResponse로 변환 성공")
    void toPageResponse_EmptyPage_Success() {
        // Given
        Page<OrderSummaryResponse> emptyPage = new PageImpl<>(
            List.of(),
            PageRequest.of(0, 10),
            0
        );

        // When
        var response = mapper.toPageResponse(emptyPage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

}
