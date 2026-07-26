package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.monolith.config.TestBeansConfiguration;
import com.msa.commerce.monolith.product.domain.ProductVariant;
import com.msa.commerce.monolith.product.domain.ProductVariantStatus;

@DataJpaTest
@Import({ProductVariantRepositoryAdapter.class, ProductVariantMapperImpl.class, TestBeansConfiguration.class})
@ActiveProfiles("test")
@DisplayName("ProductVariantRepositoryAdapter 슬라이스 테스트")
class ProductVariantRepositoryAdapterTest {

    @Autowired
    private ProductVariantRepositoryAdapter adapter;

    @Test
    @DisplayName("변형상품 저장 후 SKU로 조회된다")
    void savesAndFindsBySku() {
        ProductVariant variant = ProductVariant.builder()
            .productId(1L)
            .variantSku("SKU-1-RED-L")
            .name("Red / L")
            .priceAdjustment(new BigDecimal("1500"))
            .options(Map.of("color", "red", "size", "L"))
            .build();

        adapter.save(variant);

        Optional<ProductVariant> found = adapter.findByVariantSku("SKU-1-RED-L");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(ProductVariantStatus.ACTIVE);
        assertThat(found.get().getPriceAdjustment()).isEqualByComparingTo("1500");
        assertThat(found.get().getOptions()).containsEntry("color", "red");
    }

    @Test
    @DisplayName("상품별 변형상품 목록 조회 및 SKU 중복 확인")
    void findsByProductAndChecksDuplicate() {
        adapter.save(ProductVariant.builder().productId(2L).variantSku("SKU-2-A").name("A").build());
        adapter.save(ProductVariant.builder().productId(2L).variantSku("SKU-2-B").name("B").build());

        assertThat(adapter.findByProductId(2L)).hasSize(2);
        assertThat(adapter.existsByVariantSku("SKU-2-A")).isTrue();
        assertThat(adapter.existsByVariantSku("SKU-2-Z")).isFalse();
    }

    @Test
    @DisplayName("ID로 조회된다")
    void findsById() {
        ProductVariant saved = adapter.save(
            ProductVariant.builder().productId(3L).variantSku("SKU-3").name("V3").build());

        assertThat(adapter.findById(saved.getId())).isPresent();
    }

}
