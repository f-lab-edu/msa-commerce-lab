package com.msa.commerce.monolith.product.domain.validation;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;

@DisplayName("Notification Pattern 데모 테스트")
class NotificationPatternDemoTest {

    @Test
    @DisplayName("기존 예외 방식 vs 알림 패턴 비교 데모")
    void compareTraditionalVsNotificationPattern() {
        // given - 여러 검증 오류가 있는 잘못된 데이터
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
            .categoryId(null)           // 오류 1: null category
            .sku("")                    // 오류 2: empty SKU
            .name("")                   // 오류 3: empty name  
            .price(new BigDecimal("-100"))  // 오류 4: negative price
            .description("A".repeat(6000))  // 오류 5: too long description
            .build();

        // 1. 기존 예외 방식 - 첫 번째 오류에서만 중단됨
        System.out.println("=== 기존 예외 방식 (첫 번째 오류에서 중단) ===");
        try {
            invalidCommand.validate();
        } catch (ValidationException e) {
            System.out.println("발견된 오류: " + e.getMessage());
            System.out.println("총 오류 개수: " + e.getErrorCount());
        }

        // 2. 알림 패턴 - 모든 검증 오류를 한 번에 수집
        System.out.println("\n=== 알림 패턴 (모든 오류 수집) ===");
        Notification notification = invalidCommand.validateWithNotification();

        if (notification.hasErrors()) {
            System.out.println("총 오류 개수: " + notification.getErrorCount());
            System.out.println("모든 오류:");
            notification.getErrors().forEach(error ->
                System.out.println("  - " + error.getField() + ": " + error.getMessage())
            );
        }

        // 검증
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorCount()).isEqualTo(5);
        assertThat(notification.getErrorMessages()).contains(
            "Category ID is required",
            "SKU is required",
            "Product name is required",
            "Price must be greater than 0",
            "Product description cannot exceed 5000 characters"
        );
    }

    @Test
    @DisplayName("부분 검증 시나리오 - 업데이트 명령어")
    void partialValidationScenario() {
        // given - 일부 필드만 업데이트하는 시나리오
        System.out.println("=== 부분 검증 시나리오 (업데이트) ===");

        // when - 일부 필드는 유효하고 일부 필드는 유효하지 않음
        Notification notification = ProductValidator.validateProductUpdate(
            1L,                           // 유효한 product ID
            "VALID-SKU",                  // 유효한 SKU
            "",                           // 무효한 name (비어있음)
            new BigDecimal("1000"),       // 유효한 price
            "A".repeat(6000),             // 무효한 description (너무 긴)
            null, null, null, null, null, null, null, null, null, null
        );

        // then - 무효한 필드들만 오류로 수집됨
        System.out.println("부분 업데이트 검증 결과:");
        System.out.println("총 오류 개수: " + notification.getErrorCount());
        if (notification.hasErrors()) {
            notification.getErrors().forEach(error ->
                System.out.println("  - " + error)
            );
        }

        assertThat(notification.getErrorCount()).isEqualTo(2);
        assertThat(notification.getErrorMessages()).contains(
            "Product name is required",
            "Product description cannot exceed 5000 characters"
        );
    }

    @Test
    @DisplayName("성공적인 검증 후 처리")
    void successfulValidationHandling() {
        // given
        System.out.println("=== 성공적인 검증 시나리오 ===");

        ProductCreateCommand validCommand = ProductCreateCommand.builder()
            .categoryId(1L)
            .sku("VALID-SKU-001")
            .name("Valid Product Name")
            .price(new BigDecimal("29999"))
            .description("Valid product description")
            .shortDescription("Short desc")
            .brand("Brand")
            .model("Model")
            .build();

        // when
        Notification notification = validCommand.validateWithNotification();

        // then
        System.out.println("검증 결과: " + (notification.hasErrors() ? "실패" : "성공"));
        System.out.println("오류 개수: " + notification.getErrorCount());

        assertThat(notification.hasErrors()).isFalse();
        assertThat(notification.getErrorCount()).isEqualTo(0);

        // 성공적인 경우 비즈니스 로직 계속 진행
        System.out.println("✓ 검증 통과 - 비즈니스 로직 실행 가능");
    }

}
