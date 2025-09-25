package com.msa.commerce.orchestrator.adapter.out.persistence;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRepositoryImpl 테스트")
class OrderRepositoryImplTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @InjectMocks
    private OrderRepositoryImpl orderRepository;

    @Test
    @DisplayName("주문 번호 존재 여부 확인")
    void existsByOrderNumber_Success() {
        // given
        String orderNumber = "ORDER-001";
        when(orderJpaRepository.existsByOrderNumber(orderNumber)).thenReturn(true);

        // when
        boolean exists = orderRepository.existsByOrderNumber(orderNumber);

        // then
        assertThat(exists).isTrue();
        verify(orderJpaRepository).existsByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("상태별 주문 수 조회")
    void countByStatus_Success() {
        // given
        OrderStatus status = OrderStatus.PENDING;
        when(orderJpaRepository.countByStatus(status)).thenReturn(5L);

        // when
        long count = orderRepository.countByStatus(status);

        // then
        assertThat(count).isEqualTo(5L);
        verify(orderJpaRepository).countByStatus(status);
    }

    @Test
    @DisplayName("고객별 주문 수 조회")
    void countByCustomerId_Success() {
        // given
        Long customerId = 1L;
        when(orderJpaRepository.countByUserId(customerId)).thenReturn(3L);

        // when
        long count = orderRepository.countByCustomerId(customerId);

        // then
        assertThat(count).isEqualTo(3L);
        verify(orderJpaRepository).countByUserId(customerId);
    }

    @Test
    @DisplayName("주문 ID로 삭제")
    void deleteById_Success() {
        // given
        Long orderId = 1L;

        // when
        orderRepository.deleteById(orderId);

        // then
        verify(orderJpaRepository).deleteById(orderId);
    }

    @Test
    @DisplayName("주문 저장 시 미구현 예외")
    void save_ThrowsUnsupportedOperationException() {
        // given
        Order order = createValidOrder();

        // when & then
        assertThatThrownBy(() -> orderRepository.save(order))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Order save not yet implemented - requires domain reconstitution");
    }

    @Test
    @DisplayName("주문 조회 시 미구현 예외")
    void findById_ThrowsUnsupportedOperationException() {
        // when & then
        assertThatThrownBy(() -> orderRepository.findById(1L))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Order findById not yet implemented - requires domain reconstitution");
    }

    @Test
    @DisplayName("주문 UUID로 조회 시 미구현 예외")
    void findByOrderId_ThrowsUnsupportedOperationException() {
        // when & then
        assertThatThrownBy(() -> orderRepository.findByOrderId(UUID.randomUUID()))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Order findByOrderId not yet implemented - requires domain reconstitution");
    }

    private Order createValidOrder() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipient", "홍길동");
        shippingAddress.put("phone", "010-1234-5678");
        shippingAddress.put("addressLine1", "서울시 강남구");
        shippingAddress.put("postalCode", "06234");

        return Order.create(
            "ORDER-TEST-001",
            1L,
            shippingAddress,
            "WEB"
        );
    }
}