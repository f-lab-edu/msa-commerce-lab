package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.out.CrossDomainEventRepository;
import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;
import com.msa.commerce.orchestrator.domain.crossdomain.PublishingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CrossDomainEventRepositoryAdapter implements CrossDomainEventRepository {

    private final CrossDomainEventJpaRepository jpaRepository;

    private final CrossDomainEventMapper mapper;

    @Override
    @Transactional
    public CrossDomainEvent save(CrossDomainEvent event) {
        CrossDomainEventJpaEntity entity = (event.getId() == null)
            ? mapper.toEntity(event)
            : updateExisting(event);

        CrossDomainEventJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrossDomainEvent> findPendingEvents(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findPendingEventsWithLock(PublishingStatus.PENDING, pageable)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(PublishingStatus status) {
        return jpaRepository.countByPublishingStatus(status);
    }

    private CrossDomainEventJpaEntity updateExisting(CrossDomainEvent domain) {
        CrossDomainEventJpaEntity existing = jpaRepository.findById(domain.getId())
            .orElseThrow(() -> new IllegalArgumentException("CrossDomainEvent not found: " + domain.getId()));

        existing.updateFromDomain(domain);
        return existing;
    }

}
