package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.writer.jdbc-url=jdbc:h2:mem:testdb_writer",
    "spring.datasource.reader.jdbc-url=jdbc:h2:mem:testdb_reader",
    "logging.level.org.springframework.jdbc.core=DEBUG",
    "logging.level.com.zaxxer.hikari=DEBUG"
})
@DisplayName("데이터소스 성능 테스트 (비활성화)")
@org.junit.jupiter.api.Disabled("다중 데이터소스 통합 환경에서만 실행 - 현재는 비활성화")
class DataSourcePerformanceTest {

    @Test
    void placeholder() {
        assertThat(true).isTrue();
    }

}
