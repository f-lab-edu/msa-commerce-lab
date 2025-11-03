package com.msa.commerce.orchestrator.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.orchestrator.domain.AddressType;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.vo.ShippingAddress;

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
    @DisplayName("주문 UUID로 삭제")
    void deleteByOrderId_Success() {
        // given
        UUID orderId = UUID.randomUUID();

        // when
        orderRepository.deleteByOrderId(orderId);

        // then
        verify(orderJpaRepository).deleteById(orderId);
    }

    @Test
    @DisplayName("주문 저장 성공")
    void save_Success() {
        // given
        Order order = createValidOrder();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.save(any(OrderJpaEntity.class))).thenReturn(orderEntity);
        when(orderMapper.toDomain(any(OrderJpaEntity.class))).thenReturn(order);

        // when
        Order savedOrder = orderRepository.save(order);

        // then
        assertThat(savedOrder).isNotNull();
        verify(orderJpaRepository).save(any(OrderJpaEntity.class));
        verify(orderMapper).toDomain(any(OrderJpaEntity.class));
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
    @DisplayName("고객 ID로 주문 목록 조회 (페이징)")
    void findByCustomerId_Success() {
        // given
        Long customerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Order order1 = createValidOrder();
        Order order2 = createValidOrder();
        OrderJpaEntity entity1 = OrderJpaEntity.from(order1);
        OrderJpaEntity entity2 = OrderJpaEntity.from(order2);

        Page<OrderJpaEntity> entityPage = new PageImpl<>(List.of(entity1, entity2), pageable, 2);
        when(orderJpaRepository.findByCustomerId(customerId, pageable)).thenReturn(entityPage);
        when(orderMapper.toDomain(entity1)).thenReturn(order1);
        when(orderMapper.toDomain(entity2)).thenReturn(order2);

        // when
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);

        // then
        assertThat(orderPage.getContent()).hasSize(2);
        assertThat(orderPage.getContent()).containsExactly(order1, order2);
        assertThat(orderPage.getTotalElements()).isEqualTo(2);
        verify(orderJpaRepository).findByCustomerId(customerId, pageable);
    }

    @Test
    @DisplayName("고객 ID와 상태로 주문 목록 조회 (페이징)")
    void findByCustomerIdAndStatus_Success() {
        // given
        Long customerId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        Page<OrderJpaEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
        when(orderJpaRepository.findByCustomerIdAndStatus(customerId, status, pageable)).thenReturn(entityPage);
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        Page<Order> orderPage = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);

        // then
        assertThat(orderPage.getContent()).hasSize(1);
        assertThat(orderPage.getContent().get(0)).isEqualTo(order);
        verify(orderJpaRepository).findByCustomerIdAndStatus(customerId, status, pageable);
    }

    @Test
    @DisplayName("상태로 주문 목록 조회 (페이징)")
    void findByStatus_Success() {
        // given
        OrderStatus status = OrderStatus.CONFIRMED;
        Pageable pageable = PageRequest.of(0, 10);
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        Page<OrderJpaEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
        when(orderJpaRepository.findByStatus(status, pageable)).thenReturn(entityPage);
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);

        // then
        assertThat(orderPage.getContent()).hasSize(1);
        verify(orderJpaRepository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("기간으로 주문 목록 조회 (페이징)")
    void findOrdersByDateRange_Success() {
        // given
        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(7);
        java.time.LocalDateTime endDate = java.time.LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);
        Page<OrderJpaEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
        when(orderJpaRepository.findByOrderDateBetween(startDate, endDate, pageable)).thenReturn(entityPage);
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        Page<Order> orderPage = orderRepository.findOrdersByDateRange(startDate, endDate, pageable);

        // then
        assertThat(orderPage).hasSize(1);
        verify(orderJpaRepository).findByOrderDateBetween(startDate, endDate, pageable);
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

        when(orderJpaRepository.findByCustomerIdAndOrderDateBetween(customerId, startDate, endDate)).thenReturn(java.util.List.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // when
        List<Order> orders = orderRepository.findCustomerOrdersByDateRange(customerId, startDate, endDate);

        // then
        assertThat(orders).hasSize(1);
        verify(orderJpaRepository).findByCustomerIdAndOrderDateBetween(customerId, startDate, endDate);
    }

    @Test
    @DisplayName("UUID로 주문과 항목 함께 조회")
    void findByOrderIdWithItems_Success() {
        // given
        Order order = createValidOrder();
        UUID orderId = order.getOrderId();
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);

        when(orderJpaRepository.findWithItemsByOrderId(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toDomain(orderEntity)).thenReturn(order);

        // when
        Optional<Order> foundOrder = orderRepository.findByOrderIdWithItems(orderId);

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).isEqualTo(order);
        verify(orderJpaRepository).findWithItemsByOrderId(orderId);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    @DisplayName("UUID로 주문과 항목 함께 조회 실패 - 존재하지 않음")
    void findByOrderIdWithItems_NotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderJpaRepository.findWithItemsByOrderId(orderId)).thenReturn(Optional.empty());

        // when
        Optional<Order> foundOrder = orderRepository.findByOrderIdWithItems(orderId);

        // then
        assertThat(foundOrder).isEmpty();
        verify(orderJpaRepository).findWithItemsByOrderId(orderId);
    }

    private Order createValidOrder() {
        ShippingAddress shippingAddress = ShippingAddress.create(
            AddressType.DEFAULT,
            "홍길동",
            "010-1234-5678",
            "06234",
            "서울시 강남구",
            null
        );

        return Order.create(
            "ORDER-TEST-001",
            1L,
            shippingAddress,
            "WEB"
        );
    }

}
