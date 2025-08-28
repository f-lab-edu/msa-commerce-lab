package com.msa.commerce.monolith.product.application.port.in;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductUpdateCommand 테스트")
class ProductUpdateCommandTest {

    @Test
    @DisplayName("유효한 업데이트 명령 검증 성공")
    void validate_ValidCommand_Success() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .name("유효한 상품명")
            .description("유효한 설명")
            .price(new BigDecimal("10000"))
            .sku("VALID-SKU")
            .build();

        // when & then
        assertThatCode(command::validate)
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("상품 ID가 null일 때 예외 발생")
    void validate_NullProductId_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(null)
            .name("상품명")
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product ID is required for update.");
    }

    @Test
    @DisplayName("빈 SKU로 업데이트 시 예외 발생")
    void validate_EmptySku_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .sku("") // 빈 문자열
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SKU must not be empty and cannot exceed 100 characters.");
    }

    @Test
    @DisplayName("너무 긴 SKU로 업데이트 시 예외 발생")
    void validate_TooLongSku_ThrowsException() {
        // given
        String longSku = "A".repeat(101); // 101자
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .sku(longSku)
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SKU must not be empty and cannot exceed 100 characters.");
    }

    @Test
    @DisplayName("빈 상품명으로 업데이트 시 예외 발생")
    void validate_EmptyName_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .name("") // 빈 문자열
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name must not be empty and cannot exceed 255 characters.");
    }

    @Test
    @DisplayName("너무 긴 상품명으로 업데이트 시 예외 발생")
    void validate_TooLongName_ThrowsException() {
        // given
        String longName = "A".repeat(256); // 256자
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .name(longName)
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name must not be empty and cannot exceed 255 characters.");
    }

    @Test
    @DisplayName("0 이하의 가격으로 업데이트 시 예외 발생")
    void validate_ZeroPrice_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .price(BigDecimal.ZERO)
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price must be between 0.01 and 99,999,999.99.");
    }

    @Test
    @DisplayName("너무 높은 가격으로 업데이트 시 예외 발생")
    void validate_TooHighPrice_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .price(new BigDecimal("100000000")) // 1억
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price must be between 0.01 and 99,999,999.99.");
    }

    @Test
    @DisplayName("너무 긴 설명으로 업데이트 시 예외 발생")
    void validate_TooLongDescription_ThrowsException() {
        // given
        String longDescription = "A".repeat(5001); // 5001자
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .description(longDescription)
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product description cannot exceed 5000 characters.");
    }

    @Test
    @DisplayName("음수 재고로 업데이트 시 예외 발생")
    void validate_NegativeStock_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .initialStock(-1)
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Initial stock cannot be negative.");
    }

    @Test
    @DisplayName("잘못된 최소/최대 주문 수량으로 업데이트 시 예외 발생")
    void validate_InvalidOrderQuantity_ThrowsException() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .minOrderQuantity(10)
            .maxOrderQuantity(5) // 최대가 최소보다 작음
            .build();

        // when & then
        assertThatThrownBy(command::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Maximum order quantity cannot be less than minimum order quantity.");
    }

    @Test
    @DisplayName("변경 사항이 있는지 확인 - 있는 경우")
    void hasChanges_WithChanges_ReturnsTrue() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .name("새로운 상품명")
            .build();

        // when & then
        assertThat(command.hasChanges()).isTrue();
    }

    @Test
    @DisplayName("변경 사항이 있는지 확인 - 없는 경우")
    void hasChanges_NoChanges_ReturnsFalse() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .build();

        // when & then
        assertThat(command.hasChanges()).isFalse();
    }

    @Test
    @DisplayName("Optional 래퍼 메소드 테스트")
    void optionalMethods_Work() {
        // given
        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(1L)
            .name("테스트 상품")
            .price(new BigDecimal("10000"))
            .sku(null) // null 값
            .build();

        // when & then
        assertThat(command.getNameOptional()).isPresent();
        assertThat(command.getNameOptional().get()).isEqualTo("테스트 상품");

        assertThat(command.getPriceOptional()).isPresent();
        assertThat(command.getPriceOptional().get()).isEqualTo(new BigDecimal("10000"));

        assertThat(command.getSkuOptional()).isEmpty();
    }

}

