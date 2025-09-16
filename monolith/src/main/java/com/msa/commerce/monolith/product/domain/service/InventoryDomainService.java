package com.msa.commerce.monolith.product.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.monolith.product.adapter.out.persistence.InventoryEventJpaEntity;
import com.msa.commerce.monolith.product.adapter.out.persistence.InventoryEventJpaRepository;
import com.msa.commerce.monolith.product.adapter.out.persistence.InventorySnapshotJpaEntity;
import com.msa.commerce.monolith.product.adapter.out.persistence.InventorySnapshotJpaRepository;
import com.msa.commerce.monolith.product.adapter.out.persistence.ProductJpaEntity;
import com.msa.commerce.monolith.product.adapter.out.persistence.ProductVariantJpaEntity;
import com.msa.commerce.monolith.product.domain.InventoryEventType;
import com.msa.commerce.monolith.product.domain.StockStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 재고 관리를 위한 도메인 서비스
 * Event Sourcing 패턴을 적용하여 모든 재고 변경 이력을 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryDomainService {

    private final InventorySnapshotJpaRepository inventorySnapshotRepository;
    private final InventoryEventJpaRepository inventoryEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * 재고 입고 처리
     */
    public void stockIn(ProductJpaEntity product, ProductVariantJpaEntity variant,
                       String locationCode, int quantity, String reason,
                       String referenceType, String referenceId) {

        validateStockOperation(quantity, "입고");

        InventorySnapshotJpaEntity snapshot = getOrCreateSnapshot(product, variant, locationCode);
        int beforeQuantity = snapshot.getAvailableQuantity();

        // 재고 증가
        snapshot.adjustAvailableQuantity(quantity);
        inventorySnapshotRepository.save(snapshot);

        // 이벤트 생성
        createInventoryEvent(
            InventoryEventType.STOCK_IN,
            product, variant, locationCode,
            quantity, beforeQuantity, snapshot.getAvailableQuantity(),
            reason, referenceType, referenceId,
            createEventData("stock_in", quantity, beforeQuantity, snapshot.getAvailableQuantity())
        );

        log.info("재고 입고 처리 완료 - 상품: {}, 수량: {}, 위치: {}",
                product.getId(), quantity, locationCode);
    }

    /**
     * 재고 출고 처리
     */
    public void stockOut(ProductJpaEntity product, ProductVariantJpaEntity variant,
                        String locationCode, int quantity, String reason,
                        String referenceType, String referenceId) {

        validateStockOperation(quantity, "출고");

        InventorySnapshotJpaEntity snapshot = getOrCreateSnapshot(product, variant, locationCode);
        int beforeQuantity = snapshot.getAvailableQuantity();

        if (beforeQuantity < quantity) {
            throw new IllegalArgumentException(
                String.format("재고가 부족합니다. 현재 재고: %d, 출고 요청: %d", beforeQuantity, quantity));
        }

        // 재고 감소
        snapshot.adjustAvailableQuantity(-quantity);
        inventorySnapshotRepository.save(snapshot);

        // 이벤트 생성
        createInventoryEvent(
            InventoryEventType.STOCK_OUT,
            product, variant, locationCode,
            -quantity, beforeQuantity, snapshot.getAvailableQuantity(),
            reason, referenceType, referenceId,
            createEventData("stock_out", quantity, beforeQuantity, snapshot.getAvailableQuantity())
        );

        log.info("재고 출고 처리 완료 - 상품: {}, 수량: {}, 위치: {}",
                product.getId(), quantity, locationCode);
    }

    /**
     * 재고 예약 처리
     */
    public void reserveStock(ProductJpaEntity product, ProductVariantJpaEntity variant,
                           String locationCode, int quantity, String reason,
                           String referenceType, String referenceId) {

        validateStockOperation(quantity, "예약");

        InventorySnapshotJpaEntity snapshot = getOrCreateSnapshot(product, variant, locationCode);
        int beforeAvailable = snapshot.getAvailableQuantity();
        int beforeReserved = snapshot.getReservedQuantity();

        // 재고 예약
        snapshot.reserveStock(quantity);
        inventorySnapshotRepository.save(snapshot);

        // 이벤트 생성
        createInventoryEvent(
            InventoryEventType.STOCK_RESERVATION,
            product, variant, locationCode,
            quantity, beforeAvailable, snapshot.getAvailableQuantity(),
            reason, referenceType, referenceId,
            createEventData("stock_reservation", quantity, beforeReserved, snapshot.getReservedQuantity())
        );

        log.info("재고 예약 처리 완료 - 상품: {}, 수량: {}, 위치: {}",
                product.getId(), quantity, locationCode);
    }

    /**
     * 재고 예약 해제
     */
    public void releaseReservation(ProductJpaEntity product, ProductVariantJpaEntity variant,
                                 String locationCode, int quantity, String reason,
                                 String referenceType, String referenceId) {

        validateStockOperation(quantity, "예약해제");

        InventorySnapshotJpaEntity snapshot = getOrCreateSnapshot(product, variant, locationCode);
        int beforeReserved = snapshot.getReservedQuantity();

        // 예약 해제
        snapshot.releaseReservedStock(quantity);
        inventorySnapshotRepository.save(snapshot);

        // 이벤트 생성
        createInventoryEvent(
            InventoryEventType.STOCK_RESERVATION_RELEASE,
            product, variant, locationCode,
            quantity, beforeReserved, snapshot.getReservedQuantity(),
            reason, referenceType, referenceId,
            createEventData("reservation_release", quantity, beforeReserved, snapshot.getReservedQuantity())
        );

        log.info("재고 예약 해제 완료 - 상품: {}, 수량: {}, 위치: {}",
                product.getId(), quantity, locationCode);
    }

    /**
     * 재고 예약 확정
     */
    public void confirmReservation(ProductJpaEntity product, ProductVariantJpaEntity variant,
                                 String locationCode, int quantity, String reason,
                                 String referenceType, String referenceId) {

        validateStockOperation(quantity, "예약확정");

        InventorySnapshotJpaEntity snapshot = getOrCreateSnapshot(product, variant, locationCode);
        int beforeReserved = snapshot.getReservedQuantity();

        // 예약 확정 (예약 수량 감소)
        snapshot.confirmReservedStock(quantity);
        inventorySnapshotRepository.save(snapshot);

        // 이벤트 생성
        createInventoryEvent(
            InventoryEventType.STOCK_RESERVATION_CONFIRM,
            product, variant, locationCode,
            quantity, beforeReserved, snapshot.getReservedQuantity(),
            reason, referenceType, referenceId,
            createEventData("reservation_confirm", quantity, beforeReserved, snapshot.getReservedQuantity())
        );

        log.info("재고 예약 확정 완료 - 상품: {}, 수량: {}, 위치: {}",
                product.getId(), quantity, locationCode);
    }

    /**
     * 재고 조정 (관리자용)
     */
    public void adjustStock(ProductJpaEntity product, ProductVariantJpaEntity variant,
                          String locationCode, int adjustmentQuantity, String reason,
                          String referenceType, String referenceId) {

        if (adjustmentQuantity == 0) {
            throw new IllegalArgumentException("조정 수량은 0이 될 수 없습니다.");
        }

        InventorySnapshotJpaEntity snapshot = getOrCreateSnapshot(product, variant, locationCode);
        int beforeQuantity = snapshot.getAvailableQuantity();

        // 재고 조정
        snapshot.adjustAvailableQuantity(adjustmentQuantity);
        inventorySnapshotRepository.save(snapshot);

        // 이벤트 생성
        createInventoryEvent(
            InventoryEventType.STOCK_ADJUSTMENT,
            product, variant, locationCode,
            adjustmentQuantity, beforeQuantity, snapshot.getAvailableQuantity(),
            reason, referenceType, referenceId,
            createEventData("stock_adjustment", adjustmentQuantity, beforeQuantity, snapshot.getAvailableQuantity())
        );

        log.info("재고 조정 완료 - 상품: {}, 조정량: {}, 위치: {}",
                product.getId(), adjustmentQuantity, locationCode);
    }

    /**
     * 재고 상태 조회
     */
    @Transactional(readOnly = true)
    public StockStatus getStockStatus(ProductJpaEntity product, ProductVariantJpaEntity variant,
                                    String locationCode) {
        return inventorySnapshotRepository
            .findByProductIdAndVariantIdAndLocationCode(
                product.getId(),
                variant != null ? variant.getId() : null,
                locationCode
            )
            .map(InventorySnapshotJpaEntity::calculateStockStatus)
            .orElse(StockStatus.OUT_OF_STOCK);
    }

    /**
     * 상품별 전체 재고 조회
     */
    @Transactional(readOnly = true)
    public List<InventorySnapshotJpaEntity> getProductInventory(Long productId) {
        return inventorySnapshotRepository.findByProductIdWithProduct(productId);
    }

    /**
     * 재고 이벤트 이력 조회
     */
    @Transactional(readOnly = true)
    public List<InventoryEventJpaEntity> getInventoryHistory(Long productId, LocalDateTime since) {
        if (since != null) {
            return inventoryEventRepository.findByProductIdAndOccurredAtAfterOrderByOccurredAtDesc(productId, since);
        } else {
            return inventoryEventRepository.findByProductIdOrderByOccurredAtDesc(productId);
        }
    }

    private InventorySnapshotJpaEntity getOrCreateSnapshot(ProductJpaEntity product,
                                                         ProductVariantJpaEntity variant,
                                                         String locationCode) {
        return inventorySnapshotRepository
            .findByProductIdAndVariantIdAndLocationCode(
                product.getId(),
                variant != null ? variant.getId() : null,
                locationCode
            )
            .orElseGet(() -> createNewSnapshot(product, variant, locationCode));
    }

    private InventorySnapshotJpaEntity createNewSnapshot(ProductJpaEntity product,
                                                       ProductVariantJpaEntity variant,
                                                       String locationCode) {
        InventorySnapshotJpaEntity snapshot = InventorySnapshotJpaEntity.builder()
            .product(product)
            .variant(variant)
            .locationCode(locationCode)
            .availableQuantity(0)
            .reservedQuantity(0)
            .lowStockThreshold(10) // 기본값
            .build();

        return inventorySnapshotRepository.save(snapshot);
    }

    private void createInventoryEvent(InventoryEventType eventType, ProductJpaEntity product,
                                    ProductVariantJpaEntity variant, String locationCode,
                                    int quantityChange, int quantityBefore, int quantityAfter,
                                    String reason, String referenceType, String referenceId,
                                    String eventData) {

        String aggregateId = generateAggregateId(product.getId(),
                                               variant != null ? variant.getId() : null,
                                               locationCode);

        Long nextVersion = inventoryEventRepository.getNextVersionForAggregate(aggregateId);

        InventoryEventJpaEntity event = InventoryEventJpaEntity.builder()
            .eventType(eventType)
            .aggregateId(aggregateId)
            .aggregateVersion(nextVersion)
            .product(product)
            .variant(variant)
            .locationCode(locationCode)
            .quantityChange(quantityChange)
            .quantityBefore(quantityBefore)
            .quantityAfter(quantityAfter)
            .changeReason(reason)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .eventData(eventData)
            .correlationId(UUID.randomUUID().toString())
            .build();

        inventoryEventRepository.save(event);
    }

    private String generateAggregateId(Long productId, Long variantId, String locationCode) {
        StringBuilder aggregateId = new StringBuilder("PRODUCT_").append(productId);
        if (variantId != null) {
            aggregateId.append("_VARIANT_").append(variantId);
        }
        aggregateId.append("_").append(locationCode);
        return aggregateId.toString();
    }

    private String createEventData(String operation, int quantity, int before, int after) {
        try {
            java.util.Map<String, Object> eventData = new java.util.HashMap<>();
            eventData.put("operation", operation);
            eventData.put("quantity", quantity);
            eventData.put("before", before);
            eventData.put("after", after);
            eventData.put("timestamp", LocalDateTime.now().toString());

            return objectMapper.writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            log.warn("이벤트 데이터 직렬화 실패", e);
            return "{}";
        }
    }

    private void validateStockOperation(int quantity, String operationType) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(String.format("%s 수량은 0보다 커야 합니다.", operationType));
        }
    }
}