package com.msa.commerce.monolith.product.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.common.exception.ValidationException;
import com.msa.commerce.monolith.product.application.port.in.ProductDeleteUseCase;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.event.ProductEvent;
import com.msa.commerce.monolith.product.domain.service.InventoryDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductDeleteService implements ProductDeleteUseCase {

    private final ProductRepository productRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final InventoryDomainService inventoryDomainService;

    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId, ErrorCode.PRODUCT_NOT_FOUND.getCode()));

        validateProductDeletable(product);

        product.softDelete();
        Product deletedProduct = productRepository.save(product);

        handleProductDeletion(productId);

        applicationEventPublisher.publishEvent(ProductEvent.productDeleted(deletedProduct));
    }

    private void validateProductDeletable(Product product) {
        if (product.isDeleted()) {
            throw new ValidationException("Product is already deleted", ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode());
        }

        // 진행 중인 주문 확인
        if (hasActiveOrders(product.getId())) {
            throw new ValidationException(
                "Cannot delete product with active orders",
                ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode()
            );
        }

        // 장바구니 포함 여부 확인
        if (isInShoppingCarts(product.getId())) {
            log.warn("Product {} is in shopping carts but deletion will proceed. Cart items will become invalid.",
                product.getId());
            // 장바구니에 있어도 삭제는 진행하되, 장바구니 아이템은 무효화됨
        }
    }

    private void handleProductDeletion(Long productId) {
        // 상품 이미지 상태 변경 (실제 파일은 유지, 상태만 변경)
        try {
            handleProductImagesDeletion(productId);
        } catch (Exception e) {
            log.warn("Failed to handle product images for deletion: {}", e.getMessage());
        }

        // 재고 정보 처리 (재고는 유지하되 상태를 비활성화)
        try {
            inventoryDomainService.disableInventoryForProduct(
                productId,
                "Product deleted - inventory disabled",
                "PRODUCT_DELETION",
                productId.toString()
            );
        } catch (Exception e) {
            log.error("Failed to disable inventory for product {}: {}", productId, e.getMessage());
            // 재고 비활성화 실패는 전체 삭제를 중단시키지 않음
        }
    }

    private void handleProductImagesDeletion(Long productId) {
        // 현재 이미지 서비스가 구현되지 않았으므로 로그만 기록
        // 실제 구현 시에는 이미지 상태를 ARCHIVED 또는 DISABLED로 변경
        log.info("Product images status should be updated to DISABLED for product: {}", productId);

        // 향후 이미지 서비스 구현 시 다음과 같이 호출:
        // imageService.disableImagesForProduct(productId);
    }

    private boolean hasActiveOrders(Long productId) {
        // 현재 주문 시스템이 구현되지 않았으므로 false 반환
        // 실제 구현 시에는 주문 서비스에서 활성 주문 확인
        log.debug("Checking active orders for product: {} - Order service not implemented, returning false", productId);

        // 향후 주문 서비스 구현 시 다음과 같이 호출:
        // return orderService.hasActiveOrdersForProduct(productId);
        return false;
    }

    private boolean isInShoppingCarts(Long productId) {
        // 현재 장바구니 시스템이 구현되지 않았으므로 false 반환
        // 실제 구현 시에는 장바구니 서비스에서 포함 여부 확인
        log.debug("Checking shopping cart inclusion for product: {} - Cart service not implemented, returning false", productId);

        // 향후 장바구니 서비스 구현 시 다음과 같이 호출:
        // return cartService.isProductInAnyCarts(productId);
        return false;
    }

}
