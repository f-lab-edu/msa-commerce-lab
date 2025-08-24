package com.msa.commerce.monolith.product.adapter.in.web;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;

    private final ProductGetUseCase productGetUseCase;

    private final ProductUpdateUseCase productUpdateUseCase;

    private final ProductSearchUseCase productSearchUseCase;

    private final ProductWebMapper productWebMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productCreateUseCase.createProduct(productWebMapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long productId) {
        return ResponseEntity.ok(productGetUseCase.getProduct(productId));
    }

    @GetMapping
    public ResponseEntity<ProductPageResponse> getProducts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) @DecimalMin(value = "0.0", message = "Minimum price cannot be negative") BigDecimal minPrice,
        @RequestParam(required = false) @DecimalMin(value = "0.0", message = "Maximum price cannot be negative") BigDecimal maxPrice,
        @RequestParam(required = false) ProductStatus status,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") Integer page,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size must be at most 100") Integer size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDirection) {

        ProductSearchCommand searchCommand = ProductSearchCommand.builder()
            .categoryId(categoryId)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .status(status)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

        searchCommand.validatePriceRange();

        return ResponseEntity.ok(productSearchUseCase.searchProducts(searchCommand));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long productId,
        @Valid @RequestBody ProductUpdateRequest request) {
        ProductUpdateCommand updateCommand = productWebMapper.toUpdateCommand(productId, request);

        return ResponseEntity.ok(productUpdateUseCase.updateProduct(updateCommand));
    }

}
