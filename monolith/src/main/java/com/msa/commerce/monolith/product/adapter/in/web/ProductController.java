package com.msa.commerce.monolith.product.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;
    private final ProductUpdateUseCase productUpdateUseCase;

    private final ProductWebMapper productWebMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productCreateUseCase.createProduct(productWebMapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        
        log.info("Received product update request for ID: {} with fields: {}", 
                productId, request.hasFieldToUpdate());
        
        // 요청 검증
        if (!request.hasFieldToUpdate()) {
            log.warn("No fields to update provided for product ID: {}", productId);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            ProductResponse response = productUpdateUseCase.updateProduct(
                    productWebMapper.toUpdateCommand(productId, request));
            
            log.info("Product updated successfully for ID: {}", productId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update request for product ID: {} - {}", productId, e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (RuntimeException e) {
            log.error("Failed to update product ID: {} - {}", productId, e.getMessage());
            
            // 상품을 찾을 수 없는 경우
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            
            // 기타 서버 에러
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
