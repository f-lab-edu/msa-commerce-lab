package com.msa.commerce.monolith.product.adapter.in.web;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.domain.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 생성 요청 DTO
 * 헥사고날 아키텍처의 인바운드 어댑터
 */
@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
    private String name;

    @Size(max = 2000, message = "상품 설명은 2000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0.01", message = "가격은 0보다 커야 합니다.")
    @DecimalMax(value = "10000000", message = "가격은 1000만원을 초과할 수 없습니다.")
    @Digits(integer = 8, fraction = 2, message = "가격 형식이 올바르지 않습니다.")
    private BigDecimal price;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer stockQuantity;

    @NotNull(message = "카테고리는 필수입니다.")
    private ProductCategory category;

    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
    private String imageUrl;

    /**
     * 요청 DTO를 명령 객체로 변환
     */
    public ProductCreateCommand toCommand() {
        return ProductCreateCommand.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .imageUrl(imageUrl)
                .build();
    }
}