package com.msa.commerce.orchestrator.adapter.out.kafka;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public abstract class KafkaIntegrationTestBase {

    @Container
    protected static final KafkaContainer KAFKA_CONTAINER =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.10"))
            .withReuse(true);

    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER =
        new MySQLContainer<>(DockerImageName.parse("mysql:9.4.0"))
            .withDatabaseName("test_order_db")
            .withUsername("test")
            .withPassword("test")
            // 기본 my.cnf의 innodb_log_file_size가 MySQL 9에서 제거되어 기동에 실패하므로 오버라이드
            .withConfigurationOverride("testcontainers/mysql-conf")
            .withReuse(true);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        // 컨테이너는 빈 DB로 시작하므로 validate 대신 엔티티 기준으로 스키마를 생성한다
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

}
