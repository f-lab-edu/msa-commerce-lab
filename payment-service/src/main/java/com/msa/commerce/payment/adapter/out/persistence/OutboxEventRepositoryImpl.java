package com.msa.commerce.payment.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.payment.application.port.out.OutboxEventRepository;
import com.msa.commerce.payment.domain.outbox.OutboxEvent;
import com.msa.commerce.payment.domain.outbox.PublishingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    // 호출자의 트랜잭션(결제 상태 변경)에 참여해야 outbox 의 의미가 산다.
    @Override
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventJpaEntity entity = outboxEventJpaRepository.findByEventId(event.getEventId())
            .map(existing -> {
                existing.updateFrom(event);
                return existing;
            })
            .orElseGet(() -> OutboxEventJpaEntity.from(event));

        return outboxEventJpaRepository.save(entity).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPending(int limit) {
        return outboxEventJpaRepository
            .findByPublishingStatusOrderByOccurredAtAsc(PublishingStatus.PENDING, PageRequest.of(0, limit))
            .stream()
            .map(OutboxEventJpaEntity::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OutboxEvent> findByEventId(String eventId) {
        return outboxEventJpaRepository.findByEventId(eventId)
            .map(OutboxEventJpaEntity::toDomain);
    }

}
