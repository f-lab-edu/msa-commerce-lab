package com.msa.commerce.monolith.product.fixture;

import java.math.BigDecimal;
import java.util.stream.Stream;
import java.util.function.Supplier;

import org.junit.jupiter.params.provider.Arguments;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductType;

/**
 * Product Command 객체들의 테스트 픽스처를 제공하는 유틸리티 클래스
 * @MethodSource와 함께 사용하여 반복적인 Command 객체 생성을 줄임
 */
public class ProductCommandFixture {

    // ProductCreateCommand 관련 픽스처

    public static ProductCreateCommand validProductCreateCommand() {
        return ProductCreateCommand.builder()
            .sku("TEST-1234")
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .basePrice(new BigDecimal("10000"))
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .currency("KRW")
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .brand("TestBrand")
            .slug("test-product")
            .searchTags("test, product")
            .primaryImageUrl("https://example.com/image.jpg")
            .weightGrams(1000)
            .minOrderQuantity(1)
            .maxOrderQuantity(100)
            .build();
    }

    public static ProductCreateCommand duplicateSkuCommand() {
        return ProductCreateCommand.builder()
            .sku("DUPLICATE-SKU")
            .name("중복 SKU 테스트 상품")
            .description("중복 SKU 테스트")
            .basePrice(new BigDecimal("15000"))
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .slug("duplicate-sku-test")
            .build();
    }

    public static ProductCreateCommand duplicateNameCommand() {
        return ProductCreateCommand.builder()
            .sku("UNIQUE-SKU")
            .name("중복된 상품명")
            .description("중복 이름 테스트")
            .basePrice(new BigDecimal("20000"))
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .slug("duplicate-name-test")
            .build();
    }

    public static ProductCreateCommand invalidPriceCommand() {
        return ProductCreateCommand.builder()
            .sku("INVALID-PRICE-SKU")
            .name("잘못된 가격 상품")
            .description("가격 검증 테스트")
            .basePrice(new BigDecimal("-1000"))
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .build();
    }

    /**
     * 다양한 ProductCreateCommand 시나리오를 제공하는 Stream
     */
    public static Stream<Arguments> validCreateCommandVariations() {
        return Stream.of(
            Arguments.of("기본 물리 상품",
                ProductCreateCommand.builder()
                    .sku("PHYSICAL-001")
                    .name("물리 상품")
                    .basePrice(new BigDecimal("10000"))
                    .categoryId(1L)
                    .productType(ProductType.PHYSICAL)
                    .slug("physical-product")
                    .build()),
            Arguments.of("디지털 상품",
                ProductCreateCommand.builder()
                    .sku("DIGITAL-001")
                    .name("디지털 상품")
                    .basePrice(new BigDecimal("5000"))
                    .categoryId(2L)
                    .productType(ProductType.DIGITAL)
                    .requiresShipping(false)
                    .slug("digital-product")
                    .build()),
            Arguments.of("고가 상품",
                ProductCreateCommand.builder()
                    .sku("EXPENSIVE-001")
                    .name("고가 상품")
                    .basePrice(new BigDecimal("1000000"))
                    .categoryId(3L)
                    .productType(ProductType.PHYSICAL)
                    .isFeatured(true)
                    .slug("expensive-product")
                    .build())
        );
    }

    // ProductUpdateCommand 관련 픽스처

    public static ProductUpdateCommand validProductUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .name("업데이트된 상품명")
            .description("업데이트된 상품 설명")
            .basePrice(new BigDecimal("15000"))
            .build();
    }

    public static ProductUpdateCommand duplicateSkuUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .sku("DUPLICATE-SKU")
            .name("업데이트된 상품명")
            .build();
    }

    public static ProductUpdateCommand duplicateNameUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .name("중복된 상품명")
            .build();
    }

    public static ProductUpdateCommand partialUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .name("부분 업데이트 상품")
            .build();
    }

    public static ProductUpdateCommand emptyUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .build();
    }

    public static ProductUpdateCommand invalidPriceUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .name("잘못된 가격 업데이트")
            .basePrice(new BigDecimal("-5000"))
            .build();
    }

    public static ProductUpdateCommand invalidCategoryUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .name("잘못된 카테고리 업데이트")
            .categoryId(-1L)
            .build();
    }

    public static ProductUpdateCommand sameSkuUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .sku("ORIGINAL-SKU")
            .name("동일 SKU 업데이트")
            .build();
    }

    public static ProductUpdateCommand sameNameUpdateCommand() {
        return ProductUpdateCommand.builder()
            .productId(1L)
            .name("Original Product")
            .description("동일 이름 업데이트")
            .build();
    }

    /**
     * 다양한 ProductUpdateCommand 시나리오를 제공하는 Stream
     */
    public static Stream<Arguments> validUpdateCommandVariations() {
        return Stream.of(
            Arguments.of("이름만 업데이트",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .name("새로운 상품명")
                    .build()),
            Arguments.of("가격만 업데이트",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .basePrice(new BigDecimal("25000"))
                    .build()),
            Arguments.of("설명만 업데이트",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .description("새로운 상품 설명")
                    .build()),
            Arguments.of("전체 필드 업데이트",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .sku("NEW-SKU")
                    .name("완전히 새로운 상품")
                    .description("완전히 새로운 설명")
                    .basePrice(new BigDecimal("30000"))
                    .categoryId(2L)
                    .brand("NewBrand")
                    .build())
        );
    }

    /**
     * 잘못된 ProductCreateCommand 시나리오를 제공하는 Stream
     */
    public static Stream<Arguments> invalidCreateCommandScenarios() {
        return Stream.of(
            Arguments.of("null SKU",
                ProductCreateCommand.builder()
                    .sku(null)
                    .name("테스트 상품")
                    .basePrice(new BigDecimal("10000"))
                    .slug("test-product")
                    .build()),
            Arguments.of("빈 이름",
                ProductCreateCommand.builder()
                    .sku("TEST-001")
                    .name("")
                    .basePrice(new BigDecimal("10000"))
                    .slug("test-product")
                    .build()),
            Arguments.of("음수 가격",
                ProductCreateCommand.builder()
                    .sku("TEST-002")
                    .name("테스트 상품")
                    .basePrice(new BigDecimal("-1000"))
                    .slug("test-product")
                    .build()),
            Arguments.of("null 가격",
                ProductCreateCommand.builder()
                    .sku("TEST-003")
                    .name("테스트 상품")
                    .basePrice(null)
                    .slug("test-product")
                    .build())
        );
    }

    /**
     * 잘못된 ProductUpdateCommand 시나리오를 제공하는 Stream
     */
    public static Stream<Arguments> invalidUpdateCommandScenarios() {
        return Stream.of(
            Arguments.of("음수 가격",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .basePrice(new BigDecimal("-1000"))
                    .build()),
            Arguments.of("잘못된 카테고리 ID",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .categoryId(-1L)
                    .build()),
            Arguments.of("빈 이름",
                ProductUpdateCommand.builder()
                    .productId(1L)
                    .name("")
                    .build())
        );
    }

    // Product 도메인 검증 테스트용 픽스처

    /**
     * Product 생성자 검증 실패 시나리오를 제공하는 Stream
     */
    public static Stream<Arguments> invalidProductBuilderScenarios() {
        return Stream.of(
            Arguments.of("null 상품명",
                (Supplier<Product>) () -> Product.builder()
                    .sku("TEST-SKU")
                    .name(null)
                    .basePrice(new BigDecimal("10000"))
                    .slug("test-product")
                    .build(),
                "Product name is required."),
            Arguments.of("빈 상품명",
                (Supplier<Product>) () -> Product.builder()
                    .sku("TEST-SKU")
                    .name("")
                    .basePrice(new BigDecimal("10000"))
                    .slug("test-product")
                    .build(),
                "Product name is required."),
            Arguments.of("null SKU",
                (Supplier<Product>) () -> Product.builder()
                    .sku(null)
                    .name("테스트 상품")
                    .basePrice(new BigDecimal("10000"))
                    .slug("test-product")
                    .build(),
                "SKU is required."),
            Arguments.of("음수 가격",
                (Supplier<Product>) () -> Product.builder()
                    .sku("TEST-SKU")
                    .name("테스트 상품")
                    .basePrice(new BigDecimal("-1000"))
                    .slug("test-product")
                    .build(),
                "Base price must be greater than 0."),
            Arguments.of("최대 가격 초과",
                (Supplier<Product>) () -> Product.builder()
                    .sku("TEST-SKU")
                    .name("테스트 상품")
                    .basePrice(new BigDecimal("999999999999.9999").add(BigDecimal.ONE))
                    .slug("test-product")
                    .build(),
                "Base price cannot exceed 999,999,999,999.9999."),
            Arguments.of("상품명 길이 초과",
                (Supplier<Product>) () -> Product.builder()
                    .sku("TEST-SKU")
                    .name("A".repeat(256))
                    .basePrice(new BigDecimal("10000"))
                    .slug("test-product")
                    .build(),
                "Product name cannot exceed 255 characters.")
        );
    }

    /**
     * 주문 수량 검증 시나리오를 제공하는 Stream
     */
    public static Stream<Arguments> orderQuantityValidationScenarios() {
        Product validProduct = Product.builder()
            .sku("TEST-SKU")
            .name("테스트 상품")
            .basePrice(new BigDecimal("10000"))
            .slug("test-product")
            .minOrderQuantity(2)
            .maxOrderQuantity(10)
            .build();

        return Stream.of(
            Arguments.of("정상 수량 - 최소값", validProduct, 2, true),
            Arguments.of("정상 수량 - 중간값", validProduct, 5, true),
            Arguments.of("정상 수량 - 최대값", validProduct, 10, true),
            Arguments.of("null 수량", validProduct, null, false),
            Arguments.of("0 이하 수량", validProduct, 0, false),
            Arguments.of("음수 수량", validProduct, -1, false),
            Arguments.of("최소 수량 미만", validProduct, 1, false),
            Arguments.of("최대 수량 초과", validProduct, 11, false)
        );
    }
}