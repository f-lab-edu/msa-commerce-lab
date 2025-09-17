package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.domain.InventorySnapshot;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventorySnapshotMapper {

    public InventorySnapshot toDomain(InventorySnapshotJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return InventorySnapshot.builder()
            .id(entity.getId())
            .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
            .variantId(entity.getVariant() != null ? entity.getVariant().getId() : null)
            .locationCode(entity.getLocationCode())
            .availableQuantity(entity.getAvailableQuantity())
            .reservedQuantity(entity.getReservedQuantity())
            .lowStockThreshold(entity.getLowStockThreshold())
            .stockStatus(entity.calculateStockStatus())
            .lastUpdatedAt(entity.getLastUpdatedAt())
            .version(entity.getVersion())
            .build();
    }

    public InventorySnapshotJpaEntity toEntity(InventorySnapshot domain,
                                             ProductJpaEntity productEntity,
                                             ProductVariantJpaEntity variantEntity) {
        if (domain == null) {
            return null;
        }

        return InventorySnapshotJpaEntity.builder()
            .product(productEntity)
            .variant(variantEntity)
            .locationCode(domain.getLocationCode())
            .availableQuantity(domain.getAvailableQuantity())
            .reservedQuantity(domain.getReservedQuantity())
            .lowStockThreshold(domain.getLowStockThreshold())
            .build();
    }

    public void updateEntity(InventorySnapshotJpaEntity entity, InventorySnapshot domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.adjustAvailableQuantity(domain.getAvailableQuantity() - entity.getAvailableQuantity());

        if (domain.getReservedQuantity() != entity.getReservedQuantity()) {
            int reservedDifference = domain.getReservedQuantity() - entity.getReservedQuantity();
            if (reservedDifference > 0) {
                entity.reserveStock(reservedDifference);
            } else if (reservedDifference < 0) {
                entity.releaseReservedStock(-reservedDifference);
            }
        }

        entity.updateLowStockThreshold(domain.getLowStockThreshold());
    }

    public List<InventorySnapshot> toDomainList(List<InventorySnapshotJpaEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
            .map(this::toDomain)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    public InventorySnapshot toDomainWithValidation(InventorySnapshotJpaEntity entity) {
        InventorySnapshot domain = toDomain(entity);

        if (domain != null) {
            validateInventorySnapshot(domain);
        }

        return domain;
    }

    private void validateInventorySnapshot(InventorySnapshot snapshot) {
        if (snapshot.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException("가용 재고는 음수일 수 없습니다.");
        }

        if (snapshot.getReservedQuantity() < 0) {
            throw new IllegalArgumentException("예약 재고는 음수일 수 없습니다.");
        }

        if (snapshot.getLowStockThreshold() < 0) {
            throw new IllegalArgumentException("재고 부족 임계값은 음수일 수 없습니다.");
        }

        if (snapshot.getLocationCode() == null || snapshot.getLocationCode().trim().isEmpty()) {
            throw new IllegalArgumentException("위치 코드는 필수입니다.");
        }
    }
}