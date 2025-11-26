package com.msa.commerce.orchestrator.adapter.out.crossdomain;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaIntegrationTestBase;
import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.application.port.out.CrossDomainEventRepository;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.crossdomain.PublishingStatus;
import com.msa.commerce.orchestrator.domain.event.OrderCreatedEvent;

class CrossDomainEventRelayIntegrationTest extends KafkaIntegrationTestBase {

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private CrossDomainEventRepository crossDomainEventRepository;

    @Autowired
    private CrossDomainEventRelay crossDomainEventRelay;

    private KafkaMessageListenerContainer<String, OrderCreatedEvent> container;

    private OrderCreatedEvent receivedEvent;

    @BeforeEach
    void setUp() {
        setupConsumer();
    }

    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    @DisplayName("CrossDomain 이벤트가 Kafka로 성공적으로 발행된다")
    void shouldPublishCrossDomainEventToKafka() {
        Order order = createTestOrder();

        orderEventPublisher.publishOrderCreated(order);

        long initialPendingCount = crossDomainEventRepository.countByStatus(PublishingStatus.PENDING);
        assertThat(initialPendingCount).isEqualTo(1);

        crossDomainEventRelay.publishPendingEvents();

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(receivedEvent).isNotNull());

        assertThat(receivedEvent.getOrderId()).isEqualTo(order.getOrderId().toString());
        assertThat(receivedEvent.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(receivedEvent.getTotalAmount()).isEqualByComparingTo(order.getTotalAmount());

        await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long publishedCount = crossDomainEventRepository.countByStatus(PublishingStatus.PUBLISHED);
                assertThat(publishedCount).isEqualTo(1);
            });
    }

    @Test
    @DisplayName("여러 CrossDomain 이벤트가 순차적으로 발행된다")
    void shouldPublishMultipleCrossDomainEventsSequentially() {
        Order order1 = createTestOrder();
        Order order2 = createTestOrder();

        orderEventPublisher.publishOrderCreated(order1);
        orderEventPublisher.publishOrderCreated(order2);

        long pendingCount = crossDomainEventRepository.countByStatus(PublishingStatus.PENDING);
        assertThat(pendingCount).isEqualTo(2);

        crossDomainEventRelay.publishPendingEvents();

        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                long publishedCount = crossDomainEventRepository.countByStatus(PublishingStatus.PUBLISHED);
                assertThat(publishedCount).isEqualTo(2);
            });
    }

    private Order createTestOrder() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipientName", "홍길동");
        shippingAddress.put("address", "서울시 강남구");

        Order order = Order.create(
            "ORD-" + System.currentTimeMillis(),
            12345L,
            shippingAddress,
            "WEB"
        );

        OrderItem orderItem = OrderItem.create(
            100L,
            "테스트 상품",
            "SKU-001",
            200L,
            "옵션-1",
            2,
            new BigDecimal("10000")
        );
        order.addOrderItem(orderItem);

        return order;
    }

    private void setupConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName());

        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(KafkaTopics.ORDER_CREATED);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        container.setupMessageListener((MessageListener<String, OrderCreatedEvent>)record -> {
            receivedEvent = record.value();
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);
    }

}
