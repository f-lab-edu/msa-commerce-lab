package com.msa.commerce.orchestrator.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.ProductPort;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.vo.ProductVerification;
import com.msa.commerce.orchestrator.domain.vo.ProductVerificationItem;
import com.msa.commerce.orchestrator.domain.vo.VerifiedProduct;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderService 단위 테스트")
class CreateOrderServiceTest {

    private static final Long PRODUCT_ID = 101L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private ProductPort productPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    @DisplayName("주문 항목 정보는 클라이언트 입력이 아닌 Product Service 검증 결과로 확정된다")
    void createOrder_UsesVerifiedProductAttributes() {
        // Given
        when(productPort.verify(anyList()))
            .thenReturn(verification(true, availableProduct(new BigDecimal("25000"))));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        createOrderService.createOrder(command(2));

        // Then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        OrderItem savedItem = captor.getValue().getOrderItems().getFirst();
        assertThat(savedItem.getProductName()).isEqualTo("검증된 상품명");
        assertThat(savedItem.getProductSku()).isEqualTo("SKU-VERIFIED");
        assertThat(savedItem.getUnitPrice()).isEqualByComparingTo("25000");
    }

    @Test
    @DisplayName("주문 수량이 그대로 검증 요청으로 전달된다")
    void createOrder_PassesRequestedQuantityToVerification() {
        // Given
        when(productPort.verify(anyList()))
            .thenReturn(verification(true, availableProduct(new BigDecimal("25000"))));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        createOrderService.createOrder(command(3));

        // Then
        ArgumentCaptor<List<ProductVerificationItem>> captor = ArgumentCaptor.captor();
        verify(productPort).verify(captor.capture());

        assertThat(captor.getValue())
            .containsExactly(new ProductVerificationItem(PRODUCT_ID, 3));
    }

    @Test
    @DisplayName("구매 불가 상품이 포함되면 주문이 생성되지 않는다")
    void createOrder_ThrowsWhenProductUnavailable() {
        // Given
        VerifiedProduct unavailable = new VerifiedProduct(
            PRODUCT_ID, null, null, false, null, 0, "재고 부족");
        when(productPort.verify(anyList())).thenReturn(verification(false, unavailable));

        // When & Then
        assertThatThrownBy(() -> createOrderService.createOrder(command(2)))
            .isInstanceOf(ProductUnavailableException.class)
            .hasMessageContaining("productId=101")
            .hasMessageContaining("재고 부족");

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventPublisher, never()).publishOrderCreated(any(Order.class));
    }

    @Test
    @DisplayName("주문 저장 후 주문 생성 이벤트를 발행한다")
    void createOrder_PublishesOrderCreatedEvent() {
        // Given
        when(productPort.verify(anyList()))
            .thenReturn(verification(true, availableProduct(new BigDecimal("25000"))));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UUID orderId = createOrderService.createOrder(command(2));

        // Then
        assertThat(orderId).isNotNull();
        verify(orderEventPublisher).publishOrderCreated(any(Order.class));
    }

    private CreateOrderCommand command(int quantity) {
        return CreateOrderCommand.builder()
            .orderNumber("ORD-20260726-0001")
            .customerId(1L)
            .shippingAddress(Map.of("zipCode", "06236", "addressLine1", "서울시 강남구"))
            .sourceChannel("WEB")
            .orderItems(List.of(CreateOrderCommand.OrderItemCommand.builder()
                .productId(PRODUCT_ID)
                .quantity(quantity)
                .build()))
            .build();
    }

    private ProductVerification verification(boolean allAvailable, VerifiedProduct product) {
        return new ProductVerification(allAvailable, List.of(product));
    }

    private VerifiedProduct availableProduct(BigDecimal currentPrice) {
        return new VerifiedProduct(
            PRODUCT_ID, "SKU-VERIFIED", "검증된 상품명", true, currentPrice, 50, null);
    }

}
