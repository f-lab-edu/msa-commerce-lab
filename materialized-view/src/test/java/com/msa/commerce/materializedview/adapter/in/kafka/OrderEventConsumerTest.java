package com.msa.commerce.materializedview.adapter.in.kafka;

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

import com.msa.commerce.materializedview.adapter.in.kafka.dto.EventMetadata;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderCreatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderUpdatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.mapper.OrderViewMapper;
import com.msa.commerce.materializedview.application.port.in.UpdateOrderViewUseCase;
import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventConsumer 단위 테스트")
class OrderEventConsumerTest {

    @Mock
    private UpdateOrderViewUseCase updateOrderViewUseCase;

    @Mock
    private OrderViewMapper orderViewMapper;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    @DisplayName("order.created 수신 시 뷰 갱신 유스케이스에 위임한다")
    void delegatesOrderCreated() {
        OrderCreatedEvent event = createdEvent();
        OrderCreatedView view = new OrderCreatedView("event-1", event.orderId(), 1L,
            BigDecimal.TEN, LocalDateTime.now(), List.of());
        when(orderViewMapper.toView(event)).thenReturn(view);

        orderEventConsumer.onOrderCreated(event);

        verify(updateOrderViewUseCase).applyOrderCreated(view);
    }

    @Test
    @DisplayName("order.updated 수신 시 상태 변경 유스케이스에 위임한다")
    void delegatesOrderUpdated() {
        OrderUpdatedEvent event = updatedEvent();
        OrderStatusChangedView view = new OrderStatusChangedView("event-2", event.orderId(), 1L,
            "SHIPPED", "DELIVERED", LocalDateTime.now());
        when(orderViewMapper.toView(event)).thenReturn(view);

        orderEventConsumer.onOrderUpdated(event);

        verify(updateOrderViewUseCase).applyStatusChanged(view);
    }

    private OrderCreatedEvent createdEvent() {
        return new OrderCreatedEvent(metadata("event-1"), UUID.randomUUID(), "ORD-1", 1L,
            BigDecimal.TEN, "KRW", List.of(), "WEB", LocalDateTime.now());
    }

    private OrderUpdatedEvent updatedEvent() {
        return new OrderUpdatedEvent(metadata("event-2"), UUID.randomUUID(), "ORD-1",
            "SHIPPED", "DELIVERED", 1L, LocalDateTime.now(), null);
    }

    private EventMetadata metadata(String eventId) {
        return new EventMetadata(eventId, "corr", LocalDateTime.now(), "TYPE", "src", 1);
    }

}
