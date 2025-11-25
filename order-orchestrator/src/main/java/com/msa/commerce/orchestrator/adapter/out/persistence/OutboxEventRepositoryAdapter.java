package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.out.OutboxEventRepository;
import com.msa.commerce.orchestrator.domain.outbox.OutboxEvent;
import com.msa.commerce.orchestrator.domain.outbox.OutboxStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    @Transactional
    public OutboxEvent save(OutboxEvent outboxEvent) {
        OutboxEventJpaEntity existingEntity = outboxEventJpaRepository.findById(outboxEvent.getId())
            .orElse(null);

        if (existingEntity == null) {
            OutboxEventJpaEntity entity = OutboxEventJpaEntity.fromDomain(outboxEvent);
            OutboxEventJpaEntity savedEntity = outboxEventJpaRepository.save(entity);
            return savedEntity.toDomain();
        } else {
            existingEntity.updateFromDomain(outboxEvent);
            return existingEntity.toDomain();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingEvents(int limit) {
        return outboxEventJpaRepository
            .findPendingEventsWithLock(OutboxStatus.PENDING, PageRequest.of(0, limit))
            .stream()
            .map(OutboxEventJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OutboxStatus status) {
        return outboxEventJpaRepository.countByStatus(status);
    }

}
