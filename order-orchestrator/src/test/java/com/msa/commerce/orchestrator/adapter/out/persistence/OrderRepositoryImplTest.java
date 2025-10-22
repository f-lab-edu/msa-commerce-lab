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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRepositoryImpl 테스트")
class OrderRepositoryImplTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private OrderDomainMapper orderMapper;

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
        when(orderJpaRepository.countByCustomerId(customerId)).thenReturn(3L);

        // when
        long count = orderRepository.countByCustomerId(customerId);

        // then
        assertThat(count).isEqualTo(3L);
        verify(orderJpaRepository).countByCustomerId(customerId);
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
    @DisplayName("주문 저장 성공")
    void save_Success() {
        // given
        Order order = createValidOrder();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByOrderId(order.getOrderId())).thenReturn(java.util.Optional.empty());
        when(orderJpaRepository.save(any(OrderJpaEntity.class))).thenReturn(orderEntity);
        when(orderMapper.toDomain(any(OrderJpaEntity.class))).thenReturn(order);

        // when
        Order savedOrder = orderRepository.save(order);

        // then
        assertThat(savedOrder).isNotNull();
        verify(orderJpaRepository).findByOrderId(order.getOrderId());
        verify(orderJpaRepository).save(any(OrderJpaEntity.class));
        verify(orderMapper).toDomain(any(OrderJpaEntity.class));
    }

    @Test
    @DisplayName("ID로 주문 조회 성공")
    void findById_Success() {
        // given
        Long orderId = 1L;
        Order order = createValidOrder();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findById(orderId)).thenReturn(java.util.Optional.of(orderEntity));
        when(orderMapper.toDomain(orderEntity)).thenReturn(order);

        // when
        Optional<Order> foundOrder = orderRepository.findById(orderId);

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).isEqualTo(order);
        verify(orderJpaRepository).findById(orderId);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    @DisplayName("ID로 주문 조회 실패 - 존재하지 않음")
    void findById_NotFound() {
        // given
        Long orderId = 999L;
        when(orderJpaRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        // when
        Optional<Order> foundOrder = orderRepository.findById(orderId);

        // then
        assertThat(foundOrder).isEmpty();
        verify(orderJpaRepository).findById(orderId);
    }

    @Test
    @DisplayName("UUID로 주문 조회 성공")
    void findByOrderId_Success() {
        // given
        Order order = createValidOrder();
        UUID orderId = order.getOrderId();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByOrderId(orderId)).thenReturn(java.util.Optional.of(orderEntity));
        when(orderMapper.toDomain(orderEntity)).thenReturn(order);

        // when
        Optional<Order> foundOrder = orderRepository.findByOrderId(orderId);

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).isEqualTo(order);
        verify(orderJpaRepository).findByOrderId(orderId);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    @DisplayName("주문 번호로 조회 성공")
    void findByOrderNumber_Success() {
        // given
        Order order = createValidOrder();
        String orderNumber = order.getOrderNumber();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByOrderNumber(orderNumber)).thenReturn(java.util.Optional.of(orderEntity));
        when(orderMapper.toDomain(orderEntity)).thenReturn(order);

        // when
        Optional<Order> foundOrder = orderRepository.findByOrderNumber(orderNumber);

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).isEqualTo(order);
        verify(orderJpaRepository).findByOrderNumber(orderNumber);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    @DisplayName("고객 ID로 주문 목록 조회")
    void findByCustomerId_Success() {
        // given
        Long customerId = 1L;
        Order order1 = createValidOrder();
        Order order2 = createValidOrder();
        OrderJpaEntity entity1 = OrderJpaEntity.from(order1);
        OrderJpaEntity entity2 = OrderJpaEntity.from(order2);

        when(orderJpaRepository.findByCustomerId(customerId)).thenReturn(java.util.List.of(entity1, entity2));
        when(orderMapper.toDomain(entity1)).thenReturn(order1);
        when(orderMapper.toDomain(entity2)).thenReturn(order2);

        // when
        java.util.List<Order> orders = orderRepository.findByCustomerId(customerId);

        // then
        assertThat(orders).hasSize(2);
        assertThat(orders).containsExactly(order1, order2);
        verify(orderJpaRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("고객 ID와 상태로 주문 목록 조회")
    void findByCustomerIdAndStatus_Success() {
        // given
        Long customerId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByCustomerIdAndStatus(customerId, status)).thenReturn(java.util.List.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        java.util.List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0)).isEqualTo(order);
        verify(orderJpaRepository).findByCustomerIdAndStatus(customerId, status);
    }

    @Test
    @DisplayName("상태로 주문 목록 조회")
    void findByStatus_Success() {
        // given
        OrderStatus status = OrderStatus.CONFIRMED;
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByStatus(status)).thenReturn(java.util.List.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        java.util.List<Order> orders = orderRepository.findByStatus(status);

        // then
        assertThat(orders).hasSize(1);
        verify(orderJpaRepository).findByStatus(status);
    }

    @Test
    @DisplayName("상태로 주문 목록 조회 (생성일 역순)")
    void findByStatusOrderByCreatedAtDesc_Success() {
        // given
        OrderStatus status = OrderStatus.PAID;
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByStatusOrderByCreatedAtDesc(status)).thenReturn(java.util.List.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        java.util.List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);

        // then
        assertThat(orders).hasSize(1);
        verify(orderJpaRepository).findByStatusOrderByCreatedAtDesc(status);
    }

    @Test
    @DisplayName("기간으로 주문 목록 조회")
    void findOrdersByDateRange_Success() {
        // given
        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(7);
        java.time.LocalDateTime endDate = java.time.LocalDateTime.now();
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findOrdersByDateRange(startDate, endDate)).thenReturn(java.util.List.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        java.util.List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);

        // then
        assertThat(orders).hasSize(1);
        verify(orderJpaRepository).findOrdersByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("고객별 기간 주문 목록 조회")
    void findCustomerOrdersByDateRange_Success() {
        // given
        Long customerId = 1L;
        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(30);
        java.time.LocalDateTime endDate = java.time.LocalDateTime.now();
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findCustomerOrdersByDateRange(customerId, startDate, endDate)).thenReturn(java.util.List.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        java.util.List<Order> orders = orderRepository.findCustomerOrdersByDateRange(customerId, startDate, endDate);

        // then
        assertThat(orders).hasSize(1);
        verify(orderJpaRepository).findCustomerOrdersByDateRange(customerId, startDate, endDate);
    }

    @Test
    @DisplayName("ID로 주문과 항목 함께 조회")
    void findByIdWithItems_Success() {
        // given
        Long orderId = 1L;
        Order order = createValidOrder();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toDomain(orderEntity)).thenReturn(order);

        // when
        Optional<Order> foundOrder = orderRepository.findByIdWithItems(orderId);

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).isEqualTo(order);
        verify(orderJpaRepository).findByIdWithItems(orderId);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    @DisplayName("ID로 주문과 항목 함께 조회 실패 - 존재하지 않음")
    void findByIdWithItems_NotFound() {
        // given
        Long orderId = 999L;
        when(orderJpaRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // when
        Optional<Order> foundOrder = orderRepository.findByIdWithItems(orderId);

        // then
        assertThat(foundOrder).isEmpty();
        verify(orderJpaRepository).findByIdWithItems(orderId);
    }

    @Test
    @DisplayName("UUID로 주문과 항목 함께 조회")
    void findByOrderIdWithItems_Success() {
        // given
        Order order = createValidOrder();
        UUID orderId = order.getOrderId();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findByOrderIdWithItems(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toDomain(orderEntity)).thenReturn(order);

        // when
        Optional<Order> foundOrder = orderRepository.findByOrderIdWithItems(orderId);

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).isEqualTo(order);
        verify(orderJpaRepository).findByOrderIdWithItems(orderId);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    @DisplayName("UUID로 주문과 항목 함께 조회 실패 - 존재하지 않음")
    void findByOrderIdWithItems_NotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderJpaRepository.findByOrderIdWithItems(orderId)).thenReturn(Optional.empty());

        // when
        Optional<Order> foundOrder = orderRepository.findByOrderIdWithItems(orderId);

        // then
        assertThat(foundOrder).isEmpty();
        verify(orderJpaRepository).findByOrderIdWithItems(orderId);
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