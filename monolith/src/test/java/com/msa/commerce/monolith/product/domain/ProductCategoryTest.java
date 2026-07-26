package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductCategory 도메인 테스트")
class ProductCategoryTest {

    @Test
    @DisplayName("복원된 카테고리의 속성이 유지된다")
    void reconstitutesAllFields() {
        LocalDateTime now = LocalDateTime.now();
        ProductCategory category = ProductCategory.reconstitute(1L, null, "Electronics", "전자제품",
            "electronics", 1, true, true, "https://img/e.png", now, now);

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getSlug()).isEqualTo("electronics");
        assertThat(category.isActive()).isTrue();
        assertThat(category.isFeatured()).isTrue();
        assertThat(category.isRoot()).isTrue();
    }

    @Test
    @DisplayName("부모가 있는 카테고리는 루트가 아니다")
    void childCategoryIsNotRoot() {
        ProductCategory child = ProductCategory.reconstitute(2L, 1L, "Laptop", null,
            "laptop", 1, true, false, null, LocalDateTime.now(), LocalDateTime.now());

        assertThat(child.isRoot()).isFalse();
        assertThat(child.getParentId()).isEqualTo(1L);
    }

}
