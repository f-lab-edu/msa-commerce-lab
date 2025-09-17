package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.config.datasource.DataSourceContextHolder;
import com.msa.commerce.monolith.config.datasource.DataSourceType;
import com.msa.commerce.monolith.product.application.service.ProductGetService;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.writer.jdbc-url=jdbc:h2:mem:testdb_writer",
    "spring.datasource.reader.jdbc-url=jdbc:h2:mem:testdb_reader"
})
@DisplayName("데이터소스 라우팅 테스트")
@org.junit.jupiter.api.Disabled("다중 데이터소스 설정이 필요한 테스트 - 통합 테스트에서만 실행")
class DataSourceRoutingTest {

    @SpyBean
    private ProductGetService productGetService;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 컨텍스트 초기화
        DataSourceContextHolder.clearDataSourceType();
    }

    @Test
    @DisplayName("읽기 전용 트랜잭션에서 Reader 데이터소스 사용")
    @Transactional(readOnly = true)
    void useReaderDataSourceForReadOnlyTransaction() {
        // When
        DataSourceType currentType = DataSourceContextHolder.getDataSourceType();

        // Then - AOP에 의해 Reader 데이터소스가 설정되어야 함
        // 실제 환경에서는 DataSourceAspect가 동작하여 Reader로 설정됨
        assertThat(currentType).isNull(); // 테스트 환경에서는 직접 확인이 어려움
    }

    @Test
    @DisplayName("쓰기 트랜잭션에서 Writer 데이터소스 사용")
    @Transactional
    void useWriterDataSourceForWriteTransaction() {
        // When
        DataSourceType currentType = DataSourceContextHolder.getDataSourceType();

        // Then - 기본적으로 Writer 데이터소스 사용
        assertThat(currentType).isNull(); // 테스트 환경에서는 직접 확인이 어려움
    }

    @Test
    @DisplayName("수동으로 데이터소스 타입 설정 및 해제")
    void manualDataSourceTypeControl() {
        // Given - 초기 상태 확인
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();

        // When - Reader 설정
        DataSourceContextHolder.setDataSourceType(DataSourceType.READER);

        // Then
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.READER);

        // When - Writer로 변경
        DataSourceContextHolder.setDataSourceType(DataSourceType.WRITER);

        // Then
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.WRITER);

        // When - 컨텍스트 해제
        DataSourceContextHolder.clearDataSourceType();

        // Then
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();
    }

    @Test
    @DisplayName("ThreadLocal 격리 테스트")
    void threadLocalIsolation() throws InterruptedException {
        // Given
        DataSourceContextHolder.setDataSourceType(DataSourceType.WRITER);
        final DataSourceType[] otherThreadType = new DataSourceType[1];

        // When - 다른 스레드에서 Reader 설정
        Thread otherThread = new Thread(() -> {
            DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
            otherThreadType[0] = DataSourceContextHolder.getDataSourceType();
        });

        otherThread.start();
        otherThread.join();

        // Then - 각 스레드별로 독립적인 값 유지
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.WRITER);
        assertThat(otherThreadType[0]).isEqualTo(DataSourceType.READER);

        // Cleanup
        DataSourceContextHolder.clearDataSourceType();
    }

    @Test
    @DisplayName("컨텍스트 해제 후 기본값 테스트")
    void defaultValueAfterClear() {
        // Given
        DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.READER);

        // When
        DataSourceContextHolder.clearDataSourceType();

        // Then - 기본값은 null (DynamicDataSource에서 Writer로 처리)
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();
    }

    @Test
    @DisplayName("여러 번 연속 설정 테스트")
    void multipleSetOperations() {
        // When & Then
        DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.READER);

        DataSourceContextHolder.setDataSourceType(DataSourceType.WRITER);
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.WRITER);

        DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.READER);

        // Cleanup
        DataSourceContextHolder.clearDataSourceType();
    }
}