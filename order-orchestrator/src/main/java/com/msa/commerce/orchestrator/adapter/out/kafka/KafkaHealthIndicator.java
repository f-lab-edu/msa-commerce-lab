package com.msa.commerce.orchestrator.adapter.out.kafka;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterOptions options = new DescribeClusterOptions()
                .timeoutMs(5000);

            String clusterId = adminClient.describeCluster(options)
                .clusterId()
                .get();

            int nodeCount = adminClient.describeCluster(options)
                .nodes()
                .get()
                .size();

            return Health.up()
                .withDetail("clusterId", clusterId)
                .withDetail("nodeCount", nodeCount)
                .withDetail("topics", getTopicCount(adminClient))
                .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Kafka health check interrupted", e);
            return Health.down()
                .withException(e)
                .build();

        } catch (ExecutionException e) {
            log.error("Kafka health check failed", e);
            return Health.down()
                .withException(e)
                .withDetail("error", e.getCause().getMessage())
                .build();

        } catch (Exception e) {
            log.error("Unexpected error during Kafka health check", e);
            return Health.down()
                .withException(e)
                .build();
        }
    }

    private int getTopicCount(AdminClient adminClient) {
        try {
            return adminClient.listTopics()
                .names()
                .get()
                .size();
        } catch (Exception e) {
            log.warn("Failed to get topic count", e);
            return -1;
        }
    }

}
