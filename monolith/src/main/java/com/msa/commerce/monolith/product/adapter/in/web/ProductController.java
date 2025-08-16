package com.msa.commerce.monolith.product.adapter.in.web;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 컨트롤러
 * 헥사고날 아키텍처의 인바운드 어댑터
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product", description = "상품 관리 API")
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;

    @PostMapping
    @Operation(summary = "상품 생성", description = "새로운 상품을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "상품 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "중복된 상품명"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        
        log.info("상품 생성 요청: {}", request.getName());
        
        try {
            ProductResponse response = productCreateUseCase.createProduct(request.toCommand());
            
            log.info("상품 생성 완료: ID={}, Name={}", response.getId(), response.getName());
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
                    
        } catch (IllegalArgumentException e) {
            log.warn("상품 생성 실패 - 유효성 검증 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("상품 생성 실패 - 예상치 못한 오류", e);
            throw e;
        }
    }
}