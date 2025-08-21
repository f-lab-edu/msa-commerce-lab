package com.msa.commerce.monolith.product.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msa.commerce.monolith.product.domain.ProductInventory;

public interface ProductInventoryPort {

    /**
     * 재고 저장
     */
    ProductInventory save(ProductInventory inventory);

    /**
     * 상품과 변형으로 재고 조회
     */
    Optional<ProductInventory> findByProductIdAndProductVariantId(Long productId, Long productVariantId);

    /**
     * 상품 ID로 재고 조회
     */
    Optional<ProductInventory> findByProductId(Long productId);

    /**
     * 재고 ID로 조회
     */
    Optional<ProductInventory> findById(Long id);

    /**
     * 낮은 재고 목록 조회
     */
    List<ProductInventory> findLowStockInventories();

    /**
     * 재주문 필요 재고 목록 조회
     */
    List<ProductInventory> findReorderNeededInventories();

    /**
     * 품절 재고 목록 조회
     */
    List<ProductInventory> findOutOfStockInventories();

    /**
     * 특정 위치의 재고 목록 조회
     */
    List<ProductInventory> findByLocationCode(String locationCode);

    /**
     * 재고 추적이 활성화된 재고 목록 조회
     */
    List<ProductInventory> findByTrackingEnabled(boolean trackingEnabled);

    /**
     * 재고 삭제
     */
    void delete(ProductInventory inventory);

    /**
     * 전체 재고 수 조회
     */
    long count();

    /**
     * 특정 위치의 재고 수 조회
     */
    long countByLocationCode(String locationCode);
}