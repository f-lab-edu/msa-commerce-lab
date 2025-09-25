package com.msa.commerce.orchestrator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderOrchestratorApplication 테스트")
class OrderOrchestratorApplicationTest {

    @Test
    @DisplayName("애플리케이션 메인 클래스 존재 확인")
    void applicationClassExists() {
        // OrderOrchestratorApplication 클래스가 존재하는지 확인
        assertThat(OrderOrchestratorApplication.class).isNotNull();

        // main 메서드가 존재하는지 확인
        assertThatNoException().isThrownBy(() -> {
            OrderOrchestratorApplication.class.getDeclaredMethod("main", String[].class);
        });
    }
}