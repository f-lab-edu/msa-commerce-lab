package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.monolith.product.domain.ProductVariant;
import com.msa.commerce.monolith.product.domain.ProductVariantStatus;

import lombok.extern.slf4j.Slf4j;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Slf4j
public abstract class ProductVariantMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    // Domain 변환 (Entity -> Domain)
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "options", source = "options", qualifiedByName = "parseOptions")
    public abstract ProductVariant toDomain(ProductVariantJpaEntity entity);

    // Entity 변환 (Domain -> Entity) - default 메서드로 변경
    public ProductVariantJpaEntity toEntity(ProductVariant domain, ProductJpaEntity productEntity) {
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

    // 기본값이 적용된 Entity 변환
    public ProductVariantJpaEntity toEntityWithDefaults(ProductVariant domain, ProductJpaEntity productEntity) {
        return ProductVariantJpaEntity.builder()
            .product(productEntity)
            .variantSku(domain.getVariantSku())
            .name(domain.getName())
            .priceAdjustment(getDefaultPriceAdjustment(domain))
            .status(getDefaultStatus(domain))
            .isDefault(domain.getIsDefault())
            .options(serializeOptions(domain.getOptions()))
            .color(domain.getColor())
            .size(domain.getSize())
            .build();
    }

    // 리스트 변환 - qualifiedByName 명시로 모호성 해결
    @IterableMapping(qualifiedByName = "entityToDomain")
    public abstract List<ProductVariant> toDomainList(List<ProductVariantJpaEntity> entities);

    @Named("entityToDomain")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "options", source = "options", qualifiedByName = "parseOptions")
    public abstract ProductVariant entityToDomain(ProductVariantJpaEntity entity);

    // 업데이트 매핑 - default 메서드로 변경 (필드 직접 업데이트)
    public void updateEntityFields(ProductVariantJpaEntity entity, ProductVariant domain) {
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
    }

    // JSON 변환 메서드들
    @Named("parseOptions")
    protected Map<String, Object> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }

        try {
            return objectMapper.readValue(optionsJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("옵션 JSON 파싱 실패: {}", optionsJson, e);
            return new java.util.HashMap<>();
        }
    }

    @Named("serializeOptions")
    protected String serializeOptions(Map<String, Object> options) {
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

    // 기본값 처리 메서드들
    @Named("getDefaultPriceAdjustment")
    protected BigDecimal getDefaultPriceAdjustment(ProductVariant domain) {
        return domain.getPriceAdjustment() != null ? domain.getPriceAdjustment() : BigDecimal.ZERO;
    }

    @Named("getDefaultStatus")
    protected ProductVariantStatus getDefaultStatus(ProductVariant domain) {
        return domain.getStatus() != null ? domain.getStatus() : ProductVariantStatus.ACTIVE;
    }

    // 비즈니스 로직이 포함된 업데이트 메서드
    public void updateEntity(ProductVariantJpaEntity entity, ProductVariant domain) {
        if (entity == null || domain == null) {
            return;
        }

        // 기본 필드 업데이트
        updateEntityFields(entity, domain);

        // 비즈니스 로직 적용
        applyBusinessLogicAfterUpdate(entity, domain);
    }

    // 비즈니스 로직 후처리
    @AfterMapping
    protected void applyBusinessLogicAfterUpdate(@MappingTarget ProductVariantJpaEntity entity, ProductVariant domain) {
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

    // 검증과 함께 도메인 변환
    public ProductVariant toDomainWithValidation(ProductVariantJpaEntity entity) {
        ProductVariant domain = toDomain(entity);
        if (domain != null) {
            validateProductVariant(domain);
        }
        return domain;
    }

    // 검증 로직
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