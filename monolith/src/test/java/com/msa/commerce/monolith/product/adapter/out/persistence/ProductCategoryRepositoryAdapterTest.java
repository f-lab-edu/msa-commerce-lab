package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.monolith.config.TestBeansConfiguration;
import com.msa.commerce.monolith.product.domain.ProductCategory;

@DataJpaTest
@Import({ProductCategoryRepositoryAdapter.class, ProductCategoryMapperImpl.class, TestBeansConfiguration.class})
@ActiveProfiles("test")
@DisplayName("ProductCategoryRepositoryAdapter 슬라이스 테스트")
class ProductCategoryRepositoryAdapterTest {

    @Autowired
    private ProductCategoryRepositoryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("활성 카테고리 존재 여부를 확인한다")
    void checksActiveCategoryExistence() {
        insertCategory(1L, "Electronics", "electronics", true);
        insertCategory(2L, "Legacy", "legacy", false);

        assertThat(adapter.existsActiveById(1L)).isTrue();
        assertThat(adapter.existsActiveById(2L)).isFalse();
        assertThat(adapter.existsActiveById(99L)).isFalse();
    }

    @Test
    @DisplayName("활성 카테고리만 노출 순서대로 조회된다")
    void findsActiveCategoriesInDisplayOrder() {
        insertCategory(11L, "B-Category", "b-category", true, 2);
        insertCategory(12L, "A-Category", "a-category", true, 1);
        insertCategory(13L, "Hidden", "hidden", false, 0);

        assertThat(adapter.findAllActive())
            .extracting(ProductCategory::getName)
            .containsExactly("A-Category", "B-Category");
    }

    @Test
    @DisplayName("ID로 도메인 모델이 복원된다")
    void findsByIdAsDomain() {
        insertCategory(21L, "Fashion", "fashion", true);

        assertThat(adapter.findById(21L)).hasValueSatisfying(category -> {
            assertThat(category.getSlug()).isEqualTo("fashion");
            assertThat(category.isActive()).isTrue();
            assertThat(category.isRoot()).isTrue();
        });
    }

    private void insertCategory(Long id, String name, String slug, boolean active) {
        insertCategory(id, name, slug, active, 0);
    }

    private void insertCategory(Long id, String name, String slug, boolean active, int displayOrder) {
        jdbcTemplate.update("""
            INSERT INTO product_categories
                (id, name, slug, display_order, is_active, is_featured, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, id, name, slug, displayOrder, active);
    }

}
