package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.monolith.product.domain.ProductVariant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductVariantMapper {

    private final ObjectMapper objectMapper;

    public ProductVariant toDomain(ProductVariantJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProductVariant.builder()
            .id(entity.getId())
            .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
            .variantSku(entity.getVariantSku())
            .name(entity.getName())
            .priceAdjustment(entity.getPriceAdjustment())
            .status(entity.getStatus())
            .isDefault(entity.getIsDefault())
            .options(parseOptions(entity.getOptions()))
            .color(entity.getColor())
            .size(entity.getSize())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public ProductVariantJpaEntity toEntity(ProductVariant domain, ProductJpaEntity productEntity) {
        if (domain == null) {
            return null;
        }

        return ProductVariantJpaEntity.builder()
            .product(productEntity)
            .variantSku(domain.getVariantSku())
            .name(domain.getName())
            .priceAdjustment(domain.getPriceAdjustment())
            .status(domain.getStatus())
            .isDefault(domain.getIsDefault())
            .options(serializeOptions(domain.getOptions()))
            .color(domain.getColor())
            .size(domain.getSize())
            .build();
    }

    public void updateEntity(ProductVariantJpaEntity entity, ProductVariant domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.updateVariantInfo(
            domain.getName(),
            domain.getPriceAdjustment(),
            serializeOptions(domain.getOptions()),
            domain.getColor(),
            domain.getSize()
        );

        if (domain.getStatus() != null) {
            switch (domain.getStatus()) {
                case ACTIVE -> entity.activate();
                case INACTIVE -> entity.deactivate();
                case OUT_OF_STOCK -> entity.markOutOfStock();
            }
        }

        if (domain.getIsDefault() != null) {
            if (domain.getIsDefault()) {
                entity.setAsDefault();
            } else {
                entity.unsetAsDefault();
            }
        }
    }

    private java.util.Map<String, Object> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }

        try {
            return objectMapper.readValue(optionsJson, new TypeReference<java.util.Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("옵션 JSON 파싱 실패: {}", optionsJson, e);
            return new java.util.HashMap<>();
        }
    }

    private String serializeOptions(java.util.Map<String, Object> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            log.warn("옵션 JSON 직렬화 실패: {}", options, e);
            return null;
        }
    }

    public ProductVariantJpaEntity toEntityWithDefaults(ProductVariant domain, ProductJpaEntity productEntity) {
        ProductVariantJpaEntity entity = toEntity(domain, productEntity);

        if (entity != null) {
            // 기본 상태가 지정되지 않은 경우 ACTIVE로 설정
            if (entity.getStatus() == null) {
                entity.activate();
            }

            // 가격 조정이 null인 경우 0으로 설정
            if (entity.getPriceAdjustment() == null) {
                entity = ProductVariantJpaEntity.builder()
                    .product(entity.getProduct())
                    .variantSku(entity.getVariantSku())
                    .name(entity.getName())
                    .priceAdjustment(BigDecimal.ZERO)
                    .status(entity.getStatus())
                    .isDefault(entity.getIsDefault())
                    .options(entity.getOptions())
                    .color(entity.getColor())
                    .size(entity.getSize())
                    .build();
            }
        }

        return entity;
    }

    public java.util.List<ProductVariant> toDomainList(java.util.List<ProductVariantJpaEntity> entities) {
        if (entities == null) {
            return new java.util.ArrayList<>();
        }

        return entities.stream()
            .map(this::toDomain)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    public ProductVariant toDomainWithValidation(ProductVariantJpaEntity entity) {
        ProductVariant domain = toDomain(entity);

        if (domain != null) {
            validateProductVariant(domain);
        }

        return domain;
    }

    private void validateProductVariant(ProductVariant variant) {
        if (variant.getVariantSku() == null || variant.getVariantSku().trim().isEmpty()) {
            throw new IllegalArgumentException("변형 SKU는 필수입니다.");
        }

        if (variant.getName() == null || variant.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("변형명은 필수입니다.");
        }

        if (variant.getPriceAdjustment() != null && variant.getPriceAdjustment().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격 조정은 음수일 수 없습니다.");
        }
    }

}
