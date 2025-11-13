package com.msa.commerce.orchestrator.performance;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.PublishOrderEventPort;
import com.msa.commerce.orchestrator.application.service.CreateOrderService;
import com.msa.commerce.orchestrator.domain.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("주문 시스템 성능 테스트")
class OrderPerformanceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    private CreateOrderCommand testCommand;

    @BeforeEach
    void setUp() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");
        shippingAddress.put("street", "강남대로 123");

        CreateOrderCommand.OrderItemCommand orderItem = CreateOrderCommand.OrderItemCommand.builder()
            .productId(1001L)
            .productName("테스트 상품")
            .productSku("TEST-SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(10000))
            .build();

        testCommand = CreateOrderCommand.builder()
            .orderNumber("ORD-PERF-TEST-001")
            .customerId(12345L)
            .shippingAddress(shippingAddress)
            .sourceChannel("WEB")
            .orderItems(List.of(orderItem))
            .build();
    }

    @Test
    @DisplayName("대량 주문 생성 성능 테스트 - 100개 주문을 1초 이내 처리")
    void shouldCreateManyOrdersWithinOneSecond() {
        int orderCount = 100;
        List<UUID> createdOrderIds = new ArrayList<>();

        org.mockito.Mockito.when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return order;
            });

        Instant start = Instant.now();

        for (int i = 0; i < orderCount; i++) {
            CreateOrderCommand command = CreateOrderCommand.builder()
                .orderNumber("ORD-PERF-" + i)
                .customerId(12345L)
                .shippingAddress(testCommand.shippingAddress())
                .sourceChannel("WEB")
                .orderItems(testCommand.orderItems())
                .build();

            UUID orderId = createOrderService.createOrder(command);
            createdOrderIds.add(orderId);
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        log.info("=== 대량 주문 생성 성능 테스트 결과 ===");
        log.info("총 주문 수: {} 건", orderCount);
        log.info("총 소요 시간: {} ms", duration.toMillis());
        log.info("평균 처리 시간: {} ms/건", duration.toMillis() / orderCount);
        log.info("처리량: {} 건/초", (orderCount * 1000.0) / duration.toMillis());

        assertThat(createdOrderIds).hasSize(orderCount);
        assertThat(duration.toMillis()).isLessThan(1000);
    }

    @Test
    @DisplayName("동시성 테스트 - 10개 스레드에서 동시에 100개 주문 생성")
    void shouldHandleConcurrentOrderCreation() throws InterruptedException {
        int threadCount = 10;
        int ordersPerThread = 10;
        int totalOrders = threadCount * ordersPerThread;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        org.mockito.Mockito.when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return order;
            });

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Instant start = Instant.now();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int i = 0; i < ordersPerThread; i++) {
                    try {
                        CreateOrderCommand command = CreateOrderCommand.builder()
                            .orderNumber("ORD-THREAD-" + threadId + "-" + i)
                            .customerId(12345L)
                            .shippingAddress(testCommand.shippingAddress())
                            .sourceChannel("WEB")
                            .orderItems(testCommand.orderItems())
                            .build();

                        createOrderService.createOrder(command);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.error("Failed to create order in thread {}: {}", threadId, e.getMessage());
                    }
                }
            }, executorService);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        log.info("=== 동시성 테스트 결과 ===");
        log.info("총 스레드 수: {}", threadCount);
        log.info("스레드당 주문 수: {}", ordersPerThread);
        log.info("총 주문 수: {} 건", totalOrders);
        log.info("성공: {} 건", successCount.get());
        log.info("실패: {} 건", failureCount.get());
        log.info("총 소요 시간: {} ms", duration.toMillis());
        log.info("처리량: {} 건/초", (successCount.get() * 1000.0) / duration.toMillis());

        assertThat(successCount.get()).isEqualTo(totalOrders);
        assertThat(failureCount.get()).isZero();
    }

    @Test
    @DisplayName("주문 생성 시간 측정 - 단일 주문 생성은 10ms 이내")
    void shouldCreateSingleOrderWithinTenMilliseconds() {
        org.mockito.Mockito.when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Instant start = Instant.now();

            createOrderService.createOrder(testCommand);

            Instant end = Instant.now();
            long durationMs = Duration.between(start, end).toMillis();
            executionTimes.add(durationMs);
        }

        double avgTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);

        long maxTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        long minTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0L);

        log.info("=== 단일 주문 생성 시간 측정 결과 (10회 평균) ===");
        log.info("평균 시간: {} ms", String.format("%.2f", avgTime));
        log.info("최소 시간: {} ms", minTime);
        log.info("최대 시간: {} ms", maxTime);

        assertThat(avgTime).isLessThan(10.0);
        assertThat(maxTime).isLessThan(20);
    }

}
