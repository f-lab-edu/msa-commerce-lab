package com.msa.commerce.orchestrator.adapter.out.kafka;

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

import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.event.OrderCreatedEvent;
import com.msa.commerce.orchestrator.domain.event.OrderUpdatedEvent;

class OrderEventPublisherIntegrationTest extends KafkaIntegrationTestBase {

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    private KafkaMessageListenerContainer<String, OrderCreatedEvent> orderCreatedContainer;

    private KafkaMessageListenerContainer<String, OrderUpdatedEvent> orderUpdatedContainer;

    private OrderCreatedEvent receivedOrderCreatedEvent;

    private OrderUpdatedEvent receivedOrderUpdatedEvent;

    @BeforeEach
    void setUp() {
        setupOrderCreatedConsumer();
        setupOrderUpdatedConsumer();
    }

    @AfterEach
    void tearDown() {
        if (orderCreatedContainer != null) {
            orderCreatedContainer.stop();
        }
        if (orderUpdatedContainer != null) {
            orderUpdatedContainer.stop();
        }
    }

    @Test
    @DisplayName("주문 생성 이벤트가 성공적으로 발행되어야 한다")
    void shouldPublishOrderCreatedEvent() {
        // Given
        Order order = createTestOrder();

        // When
        orderEventPublisher.publishOrderCreated(order);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedOrderCreatedEvent).isNotNull();
                assertThat(receivedOrderCreatedEvent.getOrderId()).isEqualTo(order.getOrderId());
                assertThat(receivedOrderCreatedEvent.getOrderNumber()).isEqualTo(order.getOrderNumber());
                assertThat(receivedOrderCreatedEvent.getCustomerId()).isEqualTo(order.getCustomerId());
                assertThat(receivedOrderCreatedEvent.getTotalAmount()).isEqualByComparingTo(order.getTotalAmount());
                assertThat(receivedOrderCreatedEvent.getOrderItems()).hasSize(2);
                assertThat(receivedOrderCreatedEvent.getMetadata()).isNotNull();
                assertThat(receivedOrderCreatedEvent.getMetadata().getEventType()).isEqualTo("OrderCreated");
            });
    }

    @Test
    @DisplayName("주문 상태 변경 이벤트가 성공적으로 발행되어야 한다")
    void shouldPublishOrderUpdatedEvent() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        OrderStatus previousStatus = OrderStatus.PENDING;
        String reason = "Order confirmed by customer";

        // When
        orderEventPublisher.publishOrderUpdated(order, previousStatus, reason);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedOrderUpdatedEvent).isNotNull();
                assertThat(receivedOrderUpdatedEvent.getOrderId()).isEqualTo(order.getOrderId());
                assertThat(receivedOrderUpdatedEvent.getPreviousStatus()).isEqualTo(previousStatus);
                assertThat(receivedOrderUpdatedEvent.getCurrentStatus()).isEqualTo(OrderStatus.CONFIRMED);
                assertThat(receivedOrderUpdatedEvent.getReason()).isEqualTo(reason);
                assertThat(receivedOrderUpdatedEvent.getMetadata()).isNotNull();
                assertThat(receivedOrderUpdatedEvent.getMetadata().getEventType()).isEqualTo("OrderUpdated");
            });
    }

    private void setupOrderCreatedConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-order-created-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName());

        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(KafkaTopics.ORDER_CREATED);
        orderCreatedContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        orderCreatedContainer.setupMessageListener((MessageListener<String, OrderCreatedEvent>)record ->
            receivedOrderCreatedEvent = record.value()
        );

        orderCreatedContainer.start();
        ContainerTestUtils.waitForAssignment(orderCreatedContainer, 1);
    }

    private void setupOrderUpdatedConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-order-updated-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderUpdatedEvent.class.getName());

        DefaultKafkaConsumerFactory<String, OrderUpdatedEvent> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(KafkaTopics.ORDER_UPDATED);
        orderUpdatedContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        orderUpdatedContainer.setupMessageListener((MessageListener<String, OrderUpdatedEvent>)record ->
            receivedOrderUpdatedEvent = record.value()
        );

        orderUpdatedContainer.start();
        ContainerTestUtils.waitForAssignment(orderUpdatedContainer, 1);
    }

    private Order createTestOrder() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("street", "123 Test St");
        shippingAddress.put("city", "Seoul");
        shippingAddress.put("zipCode", "12345");

        Order order = Order.create(
            "ORD-TEST-001",
            1000L,
            shippingAddress,
            "WEB"
        );

        OrderItem item1 = OrderItem.create(
            101L,
            "Product A",
            "SKU-A",
            null,
            null,
            2,
            BigDecimal.valueOf(10000)
        );

        OrderItem item2 = OrderItem.create(
            102L,
            "Product B",
            "SKU-B",
            null,
            null,
            1,
            BigDecimal.valueOf(5000)
        );

        order.addOrderItem(item1);
        order.addOrderItem(item2);

        return order;
    }

}
