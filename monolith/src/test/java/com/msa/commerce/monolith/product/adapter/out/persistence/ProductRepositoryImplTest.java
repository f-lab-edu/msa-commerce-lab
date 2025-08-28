package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import com.msa.commerce.monolith.config.TestBeansConfiguration;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@DataJpaTest
@Import({ProductRepositoryImpl.class, TestBeansConfiguration.class})
@ActiveProfiles("test")
@DisplayName("ProductRepositoryImpl 통합 테스트")
class ProductRepositoryImplTest {

    @Autowired
    private ProductRepositoryImpl productRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    @DisplayName("카테고리별 상품 검색이 정상적으로 동작한다")
    void searchProducts_ByCategory() {
        // Given
        createTestProducts();

        ProductSearchCommand command = ProductSearchCommand.builder()
            .categoryId(1L)
            .page(0)
            .size(10)
            .sortBy("createdAt")
            .sortDirection("desc")
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
            .extracting(Product::getCategoryId)
            .containsOnly(1L);
    }

    @Test
    @DisplayName("가격 범위로 상품 검색이 정상적으로 동작한다")
    void searchProducts_ByPriceRange() {
        // Given
        createTestProducts();

        ProductSearchCommand command = ProductSearchCommand.builder()
            .minPrice(new BigDecimal("50.00"))
            .maxPrice(new BigDecimal("150.00"))
            .page(0)
            .size(10)
            .sortBy("price")
            .sortDirection("asc")
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
            .extracting(Product::getPrice)
            .allMatch(price -> price.compareTo(new BigDecimal("50.00")) >= 0 &&
                price.compareTo(new BigDecimal("150.00")) <= 0);
    }

    @Test
    @DisplayName("상품 상태로 상품 검색이 정상적으로 동작한다")
    void searchProducts_ByStatus() {
        // Given
        createTestProducts();

        ProductSearchCommand command = ProductSearchCommand.builder()
            .status(ProductStatus.ACTIVE)
            .page(0)
            .size(10)
            .sortBy("createdAt")
            .sortDirection("desc")
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent())
            .extracting(Product::getStatus)
            .containsOnly(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("복합 필터 조건으로 상품 검색이 정상적으로 동작한다")
    void searchProducts_WithMultipleFilters() {
        // Given
        createTestProducts();

        ProductSearchCommand command = ProductSearchCommand.builder()
            .categoryId(1L)
            .minPrice(new BigDecimal("90.00"))
            .maxPrice(new BigDecimal("150.00"))
            .status(ProductStatus.ACTIVE)
            .page(0)
            .size(10)
            .sortBy("price")
            .sortDirection("desc")
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(product ->
            product.getCategoryId().equals(1L) &&
                product.getPrice().compareTo(new BigDecimal("90.00")) >= 0 &&
                product.getPrice().compareTo(new BigDecimal("150.00")) <= 0 &&
                product.getStatus() == ProductStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("페이지네이션이 정상적으로 동작한다")
    void searchProducts_WithPagination() {
        // Given
        createTestProducts();

        ProductSearchCommand command = ProductSearchCommand.builder()
            .page(0)
            .size(1)
            .sortBy("createdAt")
            .sortDirection("desc")
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("정렬이 정상적으로 동작한다")
    void searchProducts_WithSorting() {
        // Given
        createTestProducts();

        ProductSearchCommand commandAsc = ProductSearchCommand.builder()
            .page(0)
            .size(10)
            .sortBy("price")
            .sortDirection("asc")
            .build();

        ProductSearchCommand commandDesc = ProductSearchCommand.builder()
            .page(0)
            .size(10)
            .sortBy("price")
            .sortDirection("desc")
            .build();

        // When
        Page<Product> resultAsc = productRepository.searchProducts(commandAsc);
        Page<Product> resultDesc = productRepository.searchProducts(commandDesc);

        // Then
        assertThat(resultAsc.getContent()).isNotEmpty();
        assertThat(resultDesc.getContent()).isNotEmpty();

        if (resultAsc.getContent().size() > 1) {
            assertThat(resultAsc.getContent().get(0).getPrice())
                .isLessThanOrEqualTo(resultAsc.getContent().get(1).getPrice());
        }

        if (resultDesc.getContent().size() > 1) {
            assertThat(resultDesc.getContent().get(0).getPrice())
                .isGreaterThanOrEqualTo(resultDesc.getContent().get(1).getPrice());
        }
    }

    private void createTestProducts() {
        LocalDateTime now = LocalDateTime.now();

        Product product1 = Product.builder()
            .categoryId(1L)
            .sku("TEST-001")
            .name("Test Product 1")
            .description("Test Description 1")
            .price(new BigDecimal("100.00"))
            .visibility("PUBLIC")
            .isFeatured(false)
            .build();

        Product product2 = Product.builder()
            .categoryId(1L)
            .sku("TEST-002")
            .name("Test Product 2")
            .description("Test Description 2")
            .price(new BigDecimal("200.00"))
            .visibility("PUBLIC")
            .isFeatured(true)
            .build();

        Product product3 = Product.builder()
            .categoryId(2L)
            .sku("TEST-003")
            .name("Test Product 3")
            .description("Test Description 3")
            .price(new BigDecimal("300.00"))
            .visibility("PRIVATE")
            .isFeatured(false)
            .build();

        productRepository.save(product1);
        productRepository.save(product2);
        Product savedProduct3 = productRepository.save(product3);

        // Set product3 to inactive after saving to test status filtering
        savedProduct3.deactivate();
        productRepository.save(savedProduct3);
    }

}

