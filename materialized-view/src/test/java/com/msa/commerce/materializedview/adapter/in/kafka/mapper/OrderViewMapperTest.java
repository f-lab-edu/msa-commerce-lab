package com.msa.commerce.materializedview.adapter.in.kafka.mapper;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.materializedview.adapter.in.kafka.dto.EventMetadata;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderCreatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderUpdatedEvent;
import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

@DisplayName("OrderViewMapper 매핑 테스트")
class OrderViewMapperTest {

    private final OrderViewMapper mapper = Mappers.getMapper(OrderViewMapper.class);

    @Test
    @DisplayName("OrderCreatedEvent가 메타데이터의 eventId와 함께 매핑된다")
    void mapsOrderCreatedEvent() {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
            metadata("event-1"), orderId, "ORD-1", 7L, new BigDecimal("50000"), "KRW",
            List.of(new OrderCreatedEvent.OrderItemData(101L, "무선 키보드", 2,
                new BigDecimal("25000"), new BigDecimal("50000"))),
            "WEB", LocalDateTime.of(2026, 7, 27, 10, 0));

        OrderCreatedView view = mapper.toView(event);

        assertThat(view.eventId()).isEqualTo("event-1");
        assertThat(view.orderId()).isEqualTo(orderId);
        assertThat(view.customerId()).isEqualTo(7L);
        assertThat(view.orderItems()).singleElement().satisfies(item -> {
            assertThat(item.productId()).isEqualTo(101L);
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.totalPrice()).isEqualByComparingTo("50000");
        });
    }

    @Test
    @DisplayName("OrderUpdatedEvent가 상태 문자열과 함께 매핑된다")
    void mapsOrderUpdatedEvent() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
            metadata("event-2"), UUID.randomUUID(), "ORD-1", "SHIPPED", "DELIVERED",
            7L, LocalDateTime.of(2026, 7, 27, 12, 0), "배송 완료");

        OrderStatusChangedView view = mapper.toView(event);

        assertThat(view.eventId()).isEqualTo("event-2");
        assertThat(view.previousStatus()).isEqualTo("SHIPPED");
        assertThat(view.currentStatus()).isEqualTo("DELIVERED");
        assertThat(view.completedDelta()).isEqualTo(1);
    }

    private EventMetadata metadata(String eventId) {
        return new EventMetadata(eventId, "corr-1", LocalDateTime.now(), "TYPE", "order-orchestrator", 1);
    }

}
