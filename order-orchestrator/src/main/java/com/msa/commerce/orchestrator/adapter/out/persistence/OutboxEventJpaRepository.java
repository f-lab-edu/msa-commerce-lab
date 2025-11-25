package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.msa.commerce.orchestrator.domain.outbox.OutboxStatus;

import jakarta.persistence.LockModeType;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboxEventJpaEntity o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEventJpaEntity> findPendingEventsWithLock(OutboxStatus status, Pageable pageable);

    long countByStatus(OutboxStatus status);

}
