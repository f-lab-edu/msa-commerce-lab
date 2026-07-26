package com.msa.commerce.payment.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.msa.commerce.payment.domain.outbox.PublishingStatus;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    List<OutboxEventJpaEntity> findByPublishingStatusOrderByOccurredAtAsc(PublishingStatus status, Pageable pageable);

    Optional<OutboxEventJpaEntity> findByEventId(String eventId);

}
