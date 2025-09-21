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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductDeleteService implements ProductDeleteUseCase {

    private final ProductRepository productRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

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

        // TODO: 진행 중인 주문 확인

        // TODO: 장바구니 포함 여부 확인
    }

    private void handleProductDeletion(Long productId) {
        // TODO: 상품 이미지 상태 변경 (실제 파일은 유지, 상태만 변경)
        // TODO: 이미지 서비스가 구현되면 호출

        // TODO: 재고 정보 처리 (재고는 유지하되 상태를 비활성화)
        // TODO: 재고 서비스가 구현되면 호출
    }

}
