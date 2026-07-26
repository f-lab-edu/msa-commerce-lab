package com.msa.commerce.materializedview.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.materializedview.application.port.out.OrderViewRepository;
import com.msa.commerce.materializedview.application.port.out.ProcessedEventPort;
import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderViewService 단위 테스트")
class OrderViewServiceTest {

    @Mock
    private ProcessedEventPort processedEventPort;

    @Mock
    private OrderViewRepository orderViewRepository;

    @InjectMocks
    private OrderViewService orderViewService;

    @Test
    @DisplayName("신규 이벤트는 뷰 갱신을 수행한다")
    void appliesNewEvent() {
        when(processedEventPort.markProcessed("event-1")).thenReturn(true);

        orderViewService.applyOrderCreated(createdView());

        verify(orderViewRepository).applyOrderCreated(any(OrderCreatedView.class));
    }

    @Test
    @DisplayName("이미 처리한 이벤트는 뷰 갱신 없이 건너뛴다")
    void skipsDuplicateEvent() {
        when(processedEventPort.markProcessed("event-1")).thenReturn(false);

        orderViewService.applyOrderCreated(createdView());

        verify(orderViewRepository, never()).applyOrderCreated(any(OrderCreatedView.class));
    }

    @Test
    @DisplayName("뷰 갱신 실패 시 처리 마크를 되돌리고 예외를 다시 던진다")
    void unmarksOnFailure() {
        when(processedEventPort.markProcessed("event-1")).thenReturn(true);
        doThrow(new IllegalStateException("db down"))
            .when(orderViewRepository).applyOrderCreated(any(OrderCreatedView.class));

        assertThatThrownBy(() -> orderViewService.applyOrderCreated(createdView()))
            .isInstanceOf(IllegalStateException.class);

        verify(processedEventPort).unmark("event-1");
    }

    @Test
    @DisplayName("상태 변경 이벤트도 동일한 멱등 처리 흐름을 따른다")
    void appliesStatusChangedEvent() {
        when(processedEventPort.markProcessed("event-2")).thenReturn(true);

        orderViewService.applyStatusChanged(statusChangedView());

        verify(orderViewRepository).applyStatusChanged(any(OrderStatusChangedView.class));
    }

    private OrderCreatedView createdView() {
        return new OrderCreatedView("event-1", UUID.randomUUID(), 1L,
            new BigDecimal("50000"), LocalDateTime.now(),
            List.of(new OrderCreatedView.OrderItemView(101L, "무선 키보드", 2, new BigDecimal("50000"))));
    }

    private OrderStatusChangedView statusChangedView() {
        return new OrderStatusChangedView("event-2", UUID.randomUUID(), 1L,
            "SHIPPED", "DELIVERED", LocalDateTime.now());
    }

}
