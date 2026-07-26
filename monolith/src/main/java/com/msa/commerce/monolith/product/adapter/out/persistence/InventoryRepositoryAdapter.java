package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.msa.commerce.monolith.product.application.port.out.InventoryRepository;
import com.msa.commerce.monolith.product.domain.InventoryEvent;
import com.msa.commerce.monolith.product.domain.InventorySnapshot;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventorySnapshotJpaRepository snapshotJpaRepository;

    private final InventoryEventJpaRepository eventJpaRepository;

    private final InventoryMapper inventoryMapper;

    @Override
    public Optional<InventorySnapshot> findForUpdate(Long productId, Long variantId, String locationCode) {
        return snapshotJpaRepository.findForUpdate(productId, variantId, locationCode)
            .map(inventoryMapper::toDomain);
    }

    @Override
    public Optional<InventorySnapshot> findOne(Long productId, Long variantId, String locationCode) {
        return snapshotJpaRepository.findOne(productId, variantId, locationCode)
            .map(inventoryMapper::toDomain);
    }

    @Override
    public InventorySnapshot save(InventorySnapshot snapshot) {
        InventorySnapshotJpaEntity saved = snapshotJpaRepository.save(inventoryMapper.toEntity(snapshot));
        return inventoryMapper.toDomain(saved);
    }

    @Override
    public List<InventorySnapshot> findByProductId(Long productId) {
        return snapshotJpaRepository.findByProductId(productId).stream()
            .map(inventoryMapper::toDomain)
            .toList();
    }

    @Override
    public void appendEvent(InventoryEvent event) {
        eventJpaRepository.save(inventoryMapper.toEntity(event));
    }

    @Override
    public List<InventoryEvent> findEventsByProductId(Long productId, LocalDateTime since) {
        return eventJpaRepository.findByProductIdAndOccurredAtAfterOrderByOccurredAtDesc(productId, since)
            .stream()
            .map(inventoryMapper::toDomain)
            .toList();
    }

}
