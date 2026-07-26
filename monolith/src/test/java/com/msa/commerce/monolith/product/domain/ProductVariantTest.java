package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductVariant 도메인 테스트")
class ProductVariantTest {

    @Test
    @DisplayName("생성 시 기본 상태는 ACTIVE, 조정가 기본값은 0")
    void createWithDefaults() {
        ProductVariant variant = ProductVariant.builder()
            .productId(1L)
            .variantSku("SKU-001-RED-L")
            .name("Red / L")
            .build();

        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
        assertThat(variant.getPriceAdjustment()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(variant.getIsDefault()).isFalse();
        assertThat(variant.isOrderable()).isTrue();
    }

    @Test
    @DisplayName("실판매가 = 기본가 + 조정가")
    void effectivePriceAddsAdjustment() {
        ProductVariant variant = ProductVariant.builder()
            .productId(1L)
            .variantSku("SKU-001-XL")
            .name("XL")
            .priceAdjustment(new BigDecimal("2000"))
            .build();

        assertThat(variant.effectivePrice(new BigDecimal("10000")))
            .isEqualByComparingTo("12000");
    }

    @Test
    @DisplayName("상태 전환: 비활성/품절 시 주문 불가")
    void statusTransitions() {
        ProductVariant variant = ProductVariant.builder()
            .productId(1L).variantSku("SKU").name("V").build();

        variant.markOutOfStock();
        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.OUT_OF_STOCK);
        assertThat(variant.isOrderable()).isFalse();

        variant.deactivate();
        assertThat(variant.isOrderable()).isFalse();

        variant.activate();
        assertThat(variant.isOrderable()).isTrue();
    }

    @Test
    @DisplayName("필수값 누락 시 예외")
    void validatesRequiredFields() {
        assertThatThrownBy(() -> ProductVariant.builder().variantSku("S").name("N").build())
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductVariant.builder().productId(1L).name("N").build())
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductVariant.builder().productId(1L).variantSku("S").build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("옵션 맵은 방어적으로 복사된다")
    void optionsAreImmutable() {
        ProductVariant variant = ProductVariant.builder()
            .productId(1L).variantSku("SKU").name("V")
            .options(Map.of("color", "red"))
            .build();

        assertThatThrownBy(() -> variant.getOptions().put("size", "L"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

}
