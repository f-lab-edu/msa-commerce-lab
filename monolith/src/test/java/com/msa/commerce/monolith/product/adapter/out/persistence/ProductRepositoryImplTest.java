package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.monolith.config.TestBeansConfiguration;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

@DataJpaTest
@Import({ProductRepositoryImpl.class, TestBeansConfiguration.class})
@ActiveProfiles("test")
@DisplayName("ProductRepositoryImpl 통합 테스트")
class ProductRepositoryImplTest {

    @Autowired
    private ProductRepositoryImpl productRepository;

    @Test
    @DisplayName("카테고리별 상품 검색이 정상적으로 동작한다")
    void searchProducts_ByCategory() {
        // Given
        createTestProducts();

        ProductSearchCommand command = ProductSearchCommand.builder()
            .categoryId(1L)
            .page(0)
            .size(10)
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
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
            .sortProperty("basePrice")
            .sortDirection(Sort.Direction.DESC)
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
            .extracting(Product::getBasePrice)
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
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
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
            .sortProperty("basePrice")
            .sortDirection(Sort.Direction.DESC)
            .build();

        // When
        Page<Product> result = productRepository.searchProducts(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(product ->
            product.getCategoryId().equals(1L) &&
                product.getBasePrice().compareTo(new BigDecimal("90.00")) >= 0 &&
                product.getBasePrice().compareTo(new BigDecimal("150.00")) <= 0 &&
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
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
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
            .sortProperty("basePrice")
            .sortDirection(Sort.Direction.ASC)
            .build();

        ProductSearchCommand commandDesc = ProductSearchCommand.builder()
            .page(0)
            .size(10)
            .sortProperty("basePrice")
            .sortDirection(Sort.Direction.DESC)
            .build();

        // When
        Page<Product> resultAsc = productRepository.searchProducts(commandAsc);
        Page<Product> resultDesc = productRepository.searchProducts(commandDesc);

        // Then
        assertThat(resultAsc.getContent()).isNotEmpty();
        assertThat(resultDesc.getContent()).isNotEmpty();

        if (resultAsc.getContent().size() > 1) {
            assertThat(resultAsc.getContent().get(0).getBasePrice())
                .isLessThanOrEqualTo(resultAsc.getContent().get(1).getBasePrice());
        }

        if (resultDesc.getContent().size() > 1) {
            assertThat(resultDesc.getContent().get(0).getBasePrice())
                .isGreaterThanOrEqualTo(resultDesc.getContent().get(1).getBasePrice());
        }
    }

    private void createTestProducts() {
        Product product1 = Product.builder()
            .sku("TEST-001")
            .name("Test Product 1")
            .description("Test Description 1")
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("100.00"))
            .slug("test-product-1")
            .isFeatured(false)
            .build();

        Product product2 = Product.builder()
            .sku("TEST-002")
            .name("Test Product 2")
            .description("Test Description 2")
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("200.00"))
            .slug("test-product-2")
            .isFeatured(true)
            .build();

        Product product3 = Product.builder()
            .sku("TEST-003")
            .name("Test Product 3")
            .description("Test Description 3")
            .categoryId(2L)
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("300.00"))
            .slug("test-product-3")
            .isFeatured(false)
            .build();

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);
        Product savedProduct3 = productRepository.save(product3);

        // Set product1 and product2 to active for status filtering tests
        savedProduct1.activate();
        savedProduct2.activate();
        productRepository.save(savedProduct1);
        productRepository.save(savedProduct2);

        // Set product3 to inactive after saving to test status filtering
        savedProduct3.deactivate();
        productRepository.save(savedProduct3);
    }

}
