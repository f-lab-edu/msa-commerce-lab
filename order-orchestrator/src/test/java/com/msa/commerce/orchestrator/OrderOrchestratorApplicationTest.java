package com.msa.commerce.orchestrator;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderOrchestratorApplication 테스트")
class OrderOrchestratorApplicationTest {

    @Test
    @DisplayName("애플리케이션 메인 클래스 존재 확인")
    void applicationClassExists() {
        assertThat(OrderOrchestratorApplication.class).isNotNull();

        assertThatNoException().isThrownBy(() -> {
            OrderOrchestratorApplication.class.getDeclaredMethod("main", String[].class);
        });
    }

}
