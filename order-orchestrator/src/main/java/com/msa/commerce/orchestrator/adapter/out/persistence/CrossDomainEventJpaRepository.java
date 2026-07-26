package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.msa.commerce.orchestrator.domain.crossdomain.PublishingStatus;

import jakarta.persistence.LockModeType;

public interface CrossDomainEventJpaRepository extends JpaRepository<CrossDomainEventJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CrossDomainEventJpaEntity c WHERE c.publishingStatus = :status ORDER BY c.createdAt ASC")
    List<CrossDomainEventJpaEntity> findPendingEventsWithLock(PublishingStatus status, Pageable pageable);

    long countByPublishingStatus(PublishingStatus status);

}
