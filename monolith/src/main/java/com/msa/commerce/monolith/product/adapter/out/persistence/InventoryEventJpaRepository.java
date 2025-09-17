package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msa.commerce.monolith.product.domain.InventoryEventType;

public interface InventoryEventJpaRepository extends JpaRepository<InventoryEventJpaEntity, Long> {

    List<InventoryEventJpaEntity> findByProductIdOrderByOccurredAtDesc(Long productId);

    List<InventoryEventJpaEntity> findByProductIdAndVariantIdOrderByOccurredAtDesc(Long productId, Long variantId);

    Page<InventoryEventJpaEntity> findByProductIdOrderByOccurredAtDesc(Long productId, Pageable pageable);

    List<InventoryEventJpaEntity> findByAggregateId(String aggregateId);

    Optional<InventoryEventJpaEntity> findByAggregateIdAndAggregateVersion(String aggregateId, Long aggregateVersion);

    List<InventoryEventJpaEntity> findByCorrelationId(String correlationId);

    @Query("""
        SELECT ie FROM InventoryEventJpaEntity ie
        WHERE ie.product.id = :productId
        AND ie.occurredAt BETWEEN :startDate AND :endDate
        ORDER BY ie.occurredAt DESC
        """)
    List<InventoryEventJpaEntity> findByProductIdAndDateRange(
        @Param("productId") Long productId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT ie FROM InventoryEventJpaEntity ie
        WHERE ie.eventType = :eventType
        AND ie.occurredAt BETWEEN :startDate AND :endDate
        ORDER BY ie.occurredAt DESC
        """)
    List<InventoryEventJpaEntity> findByEventTypeAndDateRange(
        @Param("eventType") InventoryEventType eventType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT ie FROM InventoryEventJpaEntity ie
        WHERE ie.product.id = :productId
        AND (:variantId IS NULL AND ie.variant IS NULL OR ie.variant.id = :variantId)
        AND ie.locationCode = :locationCode
        ORDER BY ie.occurredAt DESC
        """)
    List<InventoryEventJpaEntity> findByProductAndLocationOrderByOccurredAtDesc(
        @Param("productId") Long productId,
        @Param("variantId") Long variantId,
        @Param("locationCode") String locationCode);

    @Query("""
        SELECT SUM(ie.quantityChange) FROM InventoryEventJpaEntity ie
        WHERE ie.product.id = :productId
        AND (:variantId IS NULL AND ie.variant IS NULL OR ie.variant.id = :variantId)
        AND ie.locationCode = :locationCode
        AND ie.occurredAt BETWEEN :startDate AND :endDate
        """)
    Integer calculateQuantityChangeSum(
        @Param("productId") Long productId,
        @Param("variantId") Long variantId,
        @Param("locationCode") String locationCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT COUNT(ie) FROM InventoryEventJpaEntity ie
        WHERE ie.eventType = :eventType
        AND ie.occurredAt BETWEEN :startDate AND :endDate
        """)
    long countByEventTypeAndDateRange(
        @Param("eventType") InventoryEventType eventType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT ie FROM InventoryEventJpaEntity ie
        WHERE ie.referenceType = :referenceType
        AND ie.referenceId = :referenceId
        ORDER BY ie.occurredAt DESC
        """)
    List<InventoryEventJpaEntity> findByReference(
        @Param("referenceType") String referenceType,
        @Param("referenceId") String referenceId);

    @Query("""
        SELECT ie FROM InventoryEventJpaEntity ie
        WHERE ie.aggregateId = :aggregateId
        AND ie.aggregateVersion > :fromVersion
        ORDER BY ie.aggregateVersion ASC
        """)
    List<InventoryEventJpaEntity> findEventsAfterVersion(
        @Param("aggregateId") String aggregateId,
        @Param("fromVersion") Long fromVersion);

    boolean existsByCorrelationId(String correlationId);

    @Query("""
        SELECT MAX(ie.aggregateVersion) FROM InventoryEventJpaEntity ie
        WHERE ie.aggregateId = :aggregateId
        """)
    Long findMaxVersionByAggregateId(@Param("aggregateId") String aggregateId);

    @Query("""
        SELECT ie FROM InventoryEventJpaEntity ie
        JOIN FETCH ie.product p
        LEFT JOIN FETCH ie.variant v
        WHERE ie.locationCode = :locationCode
        AND ie.occurredAt >= :since
        ORDER BY ie.occurredAt DESC
        """)
    List<InventoryEventJpaEntity> findRecentEventsByLocationWithDetails(
        @Param("locationCode") String locationCode,
        @Param("since") LocalDateTime since);

    List<InventoryEventJpaEntity> findByProductIdAndOccurredAtAfterOrderByOccurredAtDesc(Long productId, LocalDateTime since);

    @Query("""
        SELECT COALESCE(MAX(ie.aggregateVersion), 0) + 1
        FROM InventoryEventJpaEntity ie
        WHERE ie.aggregateId = :aggregateId
        """)
    Long getNextVersionForAggregate(@Param("aggregateId") String aggregateId);
}