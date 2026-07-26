package com.msa.commerce.payment.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.payment.domain.outbox.OutboxEvent;
import com.msa.commerce.payment.domain.outbox.PublishingStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventRepositoryImpl 단위 테스트")
class OutboxEventRepositoryImplTest {

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @InjectMocks
    private OutboxEventRepositoryImpl outboxEventRepository;

    @Test
    @DisplayName("새 이벤트는 엔티티를 새로 만들어 저장한다")
    void insertsNewEvent() {
        OutboxEvent event = pendingEvent();
        given(outboxEventJpaRepository.findByEventId("event-1")).willReturn(Optional.empty());
        given(outboxEventJpaRepository.save(any(OutboxEventJpaEntity.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        OutboxEvent saved = outboxEventRepository.save(event);

        assertThat(saved.getEventId()).isEqualTo("event-1");
        assertThat(saved.getPublishingStatus()).isEqualTo(PublishingStatus.PENDING);
    }

    @Test
    @DisplayName("이미 있는 이벤트는 기존 엔티티의 발행 상태만 갱신한다")
    void updatesExistingEvent() {
        OutboxEvent event = pendingEvent();
        OutboxEventJpaEntity existing = OutboxEventJpaEntity.from(event);
        given(outboxEventJpaRepository.findByEventId("event-1")).willReturn(Optional.of(existing));
        given(outboxEventJpaRepository.save(any(OutboxEventJpaEntity.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        event.markAsPublished(1, 10L);
        OutboxEvent saved = outboxEventRepository.save(event);

        ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(outboxEventJpaRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(saved.getPublishingStatus()).isEqualTo(PublishingStatus.PUBLISHED);
        assertThat(saved.getKafkaOffset()).isEqualTo(10L);
    }

    @Test
    @DisplayName("PENDING 이벤트를 발생 순서대로 제한된 개수만 가져온다")
    void findsPendingEventsInOrder() {
        given(outboxEventJpaRepository.findByPublishingStatusOrderByOccurredAtAsc(
            eq(PublishingStatus.PENDING), any(Pageable.class)))
            .willReturn(List.of(OutboxEventJpaEntity.from(pendingEvent())));

        assertThat(outboxEventRepository.findPending(50)).hasSize(1);

        verify(outboxEventJpaRepository)
            .findByPublishingStatusOrderByOccurredAtAsc(PublishingStatus.PENDING, PageRequest.of(0, 50));
    }

    @Test
    @DisplayName("eventId 로 조회할 수 있다")
    void findsByEventId() {
        given(outboxEventJpaRepository.findByEventId("event-1"))
            .willReturn(Optional.of(OutboxEventJpaEntity.from(pendingEvent())));

        assertThat(outboxEventRepository.findByEventId("event-1"))
            .get()
            .extracting(OutboxEvent::getAggregateId)
            .isEqualTo("order-1");
    }

    private OutboxEvent pendingEvent() {
        return OutboxEvent.pending("event-1", "PAYMENT_RESULT", "order-1", "payment.result", "{}", "corr-1");
    }

}
