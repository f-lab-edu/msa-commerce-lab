package com.msa.commerce.monolith.product.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;

    private final ProductGetUseCase productGetUseCase;

    private final ProductUpdateUseCase productUpdateUseCase;

    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productCreateUseCase.createProduct(productMapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long productId) {
        return ResponseEntity.ok(productGetUseCase.getProduct(productId));
    }

    @GetMapping
    public ResponseEntity<ProductPageResponse> getProducts(@Valid @ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(productGetUseCase.searchProducts(productMapper.toSearchCommand(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long productId,
        @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productUpdateUseCase.updateProduct(productMapper.toUpdateCommand(productId, request)));
    }

}
