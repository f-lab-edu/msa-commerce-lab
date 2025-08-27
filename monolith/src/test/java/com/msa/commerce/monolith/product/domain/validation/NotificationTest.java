package com.msa.commerce.monolith.product.domain.validation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Notification 테스트")
class NotificationTest {

    @Test
    @DisplayName("성공적인 알림 생성")
    void createSuccessfulNotification() {
        // when
        Notification notification = Notification.success();

        // then
        assertThat(notification.hasErrors()).isFalse();
        assertThat(notification.getErrorCount()).isEqualTo(0);
        assertThat(notification.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("단일 오류 추가")
    void addSingleError() {
        // given
        Notification notification = new Notification();

        // when
        notification.addError("field1", "Error message 1");

        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorCount()).isEqualTo(1);
        assertThat(notification.getErrorMessages()).containsExactly("Error message 1");
        assertThat(notification.getErrors()).hasSize(1);
        assertThat(notification.getErrors().get(0).getField()).isEqualTo("field1");
        assertThat(notification.getErrors().get(0).getMessage()).isEqualTo("Error message 1");
    }

    @Test
    @DisplayName("다중 오류 추가")
    void addMultipleErrors() {
        // given
        Notification notification = new Notification();

        // when
        notification.addError("field1", "Error message 1")
            .addError("field2", "Error message 2")
            .addError("General error");

        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorCount()).isEqualTo(3);
        assertThat(notification.getErrorMessages()).containsExactly(
            "Error message 1", "Error message 2", "General error"
        );
        assertThat(notification.getErrorText()).isEqualTo("Error message 1; Error message 2; General error");
    }

    @Test
    @DisplayName("오류가 있을 때 예외 발생")
    void throwIfHasErrors() {
        // given
        Notification notification = new Notification();
        notification.addError("field1", "Error message 1");

        // when & then
        assertThatThrownBy(() -> notification.throwIfHasErrors())
            .isInstanceOf(ValidationException.class)
            .hasMessage("Error message 1")
            .satisfies(ex -> {
                ValidationException validationEx = (ValidationException)ex;
                assertThat(validationEx.getErrors()).hasSize(1);
                assertThat(validationEx.getErrorCount()).isEqualTo(1);
            });
    }

    @Test
    @DisplayName("오류가 없을 때 예외 발생하지 않음")
    void noThrowIfNoErrors() {
        // given
        Notification notification = new Notification();

        // when & then
        assertThatCode(() -> notification.throwIfHasErrors())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("알림 지우기")
    void clearNotification() {
        // given
        Notification notification = new Notification();
        notification.addError("field1", "Error message 1");

        // when
        notification.clear();

        // then
        assertThat(notification.hasErrors()).isFalse();
        assertThat(notification.getErrorCount()).isEqualTo(0);
        assertThat(notification.getErrors()).isEmpty();
    }

}
