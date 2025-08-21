package com.msa.commerce.common.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        ReflectionTestUtils.setField(exceptionHandler, "activeProfile", "test");

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/v1/test");
        request = mockRequest;
    }

    @Test
    @DisplayName("BusinessException 처리 테스트")
    void handleBusinessException() {
        // given
        BusinessException exception = new BusinessException("Test business error", "B001");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Test business error");
        assertThat(response.getBody().getCode()).isEqualTo("B001");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("DuplicateResourceException 처리 테스트")
    void handleDuplicateResourceException() {
        // given
        DuplicateResourceException exception = new DuplicateResourceException(
            "Resource already exists",
            ErrorCode.PRODUCT_NAME_DUPLICATE.getCode()
        );

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResourceException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource already exists");
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.PRODUCT_NAME_DUPLICATE.getCode());
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("ResourceNotFoundException 처리 테스트")
    void handleResourceNotFoundException() {
        // given
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found", "R001");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getBody().getCode()).isEqualTo("R001");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 테스트")
    void handleIllegalArgumentException() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    @DisplayName("일반 Exception 처리 테스트")
    void handleGenericException() {
        // given
        Exception exception = new RuntimeException("Unexpected error");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getDebugInfo()).contains("RuntimeException");
    }

    @Test
    @DisplayName("프로덕션 환경에서 디버그 정보 제외 테스트")
    void handleExceptionInProductionEnvironment() {
        // given
        ReflectionTestUtils.setField(exceptionHandler, "activeProfile", "prod");
        Exception exception = new RuntimeException("Unexpected error");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDebugInfo()).isNull();
    }

}
