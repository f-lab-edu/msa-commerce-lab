package com.msa.commerce.orchestrator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderOrchestratorApplication 테스트")
class OrderOrchestratorApplicationTest {

    @Test
    @DisplayName("스프링 컨텍스트 로딩")
    void contextLoads() {
        // Spring Boot application context가 정상적으로 로딩되는지 확인
        // 별도의 assertion 없이도 컨텍스트 로딩 실패 시 테스트가 실패함
    }
}