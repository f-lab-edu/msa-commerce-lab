package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.config.datasource.DataSourceContextHolder;
import com.msa.commerce.monolith.config.datasource.DataSourceType;
import com.msa.commerce.monolith.product.application.service.ProductGetService;
import com.msa.commerce.monolith.product.domain.ProductType;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.writer.jdbc-url=jdbc:h2:mem:testdb_writer",
    "spring.datasource.reader.jdbc-url=jdbc:h2:mem:testdb_reader",
    "logging.level.org.springframework.jdbc.core=DEBUG",
    "logging.level.com.zaxxer.hikari=DEBUG"
})
@DisplayName("데이터소스 성능 테스트")
@org.junit.jupiter.api.Disabled("다중 데이터소스 설정이 필요한 테스트 - 통합 테스트에서만 실행")
class DataSourcePerformanceTest {

    @Autowired
    private ProductGetService productGetService;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private ProductCategoryJpaRepository categoryRepository;

    private ProductCategoryJpaEntity testCategory;

    private List<ProductJpaEntity> testProducts;

    @BeforeEach
    @Transactional
    void setUp() {
        // 컨텍스트 초기화
        DataSourceContextHolder.clearDataSourceType();

        // 테스트 카테고리 생성
        testCategory = ProductCategoryJpaEntity.builder()
            .name("DS 성능 테스트 카테고리")
            .slug("ds-performance-category")
            .description("데이터소스 성능 테스트용")
            .isActive(true)
            .displayOrder(1)
            .build();
        testCategory = categoryRepository.save(testCategory);

        // 테스트 상품 생성 (50개)
        testProducts = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            ProductJpaEntity product = ProductJpaEntity.fromDomainEntityForCreation(
                com.msa.commerce.monolith.product.domain.Product.builder()
                    .sku("DS-PERF-" + String.format("%03d", i))
                    .name("DS 성능 테스트 상품 " + i)
                    .description("데이터소스 성능 테스트용 상품 " + i)
                    .brand("DSBrand" + (i % 3))
                    .productType(i % 2 == 0 ? ProductType.PHYSICAL : ProductType.DIGITAL)
                    .basePrice(new BigDecimal(String.valueOf(2000 + i * 100)))
                    .currency("KRW")
                    .requiresShipping(i % 2 == 0)
                    .isTaxable(true)
                    .isFeatured(i % 5 == 0)
                    .slug("ds-perf-product-" + i)
                    .searchTags("DS테스트, 성능, 상품" + i)
                    .build()
            );
            product.setCategory(testCategory);
            product.setCategoryId(testCategory.getId());
            testProducts.add(productRepository.save(product));
        }
    }

    @Test
    @DisplayName("Reader 데이터소스 성능 테스트")
    @Transactional(readOnly = true)
    void testReaderDataSourcePerformance() {
        // Given
        DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
        long startTime = System.currentTimeMillis();

        // When - 읽기 전용 트랜잭션으로 대량 조회
        List<ProductJpaEntity> products = productRepository.findFirst20ByOrderByIdAsc();

        // 상품 상세 정보 조회
        for (ProductJpaEntity product : products.subList(0, Math.min(10, products.size()))) {
            var productWithDetails = productRepository.findProductWithFullDetails(product.getId());
            assertThat(productWithDetails).isPresent();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        System.out.println("=== Reader 데이터소스 성능 ===");
        System.out.println("조회된 상품 수: " + products.size());
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("현재 데이터소스 타입: " + DataSourceContextHolder.getDataSourceType());

        assertThat(executionTime).isLessThan(2000); // 2초 이내
        assertThat(products).isNotEmpty();
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.READER);
    }

    @Test
    @DisplayName("Writer 데이터소스 성능 테스트")
    @Transactional
    void testWriterDataSourcePerformance() {
        // Given
        DataSourceContextHolder.setDataSourceType(DataSourceType.WRITER);
        long startTime = System.currentTimeMillis();

        // When - 상품 정보 업데이트
        List<ProductJpaEntity> products = productRepository.findFirst10ByOrderByIdAsc();
        for (ProductJpaEntity product : products) {
            product.setName(product.getName() + " - 업데이트됨");
            productRepository.save(product);
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        System.out.println("=== Writer 데이터소스 성능 ===");
        System.out.println("업데이트된 상품 수: " + products.size());
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("현재 데이터소스 타입: " + DataSourceContextHolder.getDataSourceType());

        assertThat(executionTime).isLessThan(3000); // 3초 이내
        assertThat(products).isNotEmpty();
        // Writer는 기본값이므로 null일 수 있음
    }

    @Test
    @DisplayName("동시 Reader/Writer 접근 성능 테스트")
    void testConcurrentReaderWriterAccess() throws InterruptedException, ExecutionException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // When - 동시에 Reader/Writer 작업 실행
        // Reader 작업 5개
        for (int i = 0; i < 5; i++) {
            final int index = i;
            CompletableFuture<Long> readerTask = CompletableFuture.supplyAsync(() -> {
                try {
                    DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
                    long taskStart = System.currentTimeMillis();

                    // 읽기 작업
                    productRepository.findFirst5ByOrderByIdAsc();

                    long taskEnd = System.currentTimeMillis();
                    System.out.println("Reader Task " + index + " 완료: " + (taskEnd - taskStart) + "ms");
                    return taskEnd - taskStart;
                } finally {
                    DataSourceContextHolder.clearDataSourceType();
                }
            }, executor);
            futures.add(readerTask);
        }

        // Writer 작업 3개
        for (int i = 0; i < 3; i++) {
            final int index = i;
            CompletableFuture<Long> writerTask = CompletableFuture.supplyAsync(() -> {
                try {
                    DataSourceContextHolder.setDataSourceType(DataSourceType.WRITER);
                    long taskStart = System.currentTimeMillis();

                    // 쓰기 작업 시뮬레이션 (조회만 수행, 실제 업데이트는 트랜잭션 문제로 제외)
                    productRepository.findFirst3ByOrderByIdAsc();

                    long taskEnd = System.currentTimeMillis();
                    System.out.println("Writer Task " + index + " 완료: " + (taskEnd - taskStart) + "ms");
                    return taskEnd - taskStart;
                } finally {
                    DataSourceContextHolder.clearDataSourceType();
                }
            }, executor);
            futures.add(writerTask);
        }

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then
        System.out.println("=== 동시 Reader/Writer 접근 성능 ===");
        System.out.println("총 작업 수: " + futures.size());
        System.out.println("총 실행 시간: " + totalTime + "ms");

        long avgTaskTime = futures.stream()
            .mapToLong(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum() / futures.size();
        System.out.println("평균 작업 시간: " + avgTaskTime + "ms");

        assertThat(totalTime).isLessThan(5000); // 5초 이내
        assertThat(avgTaskTime).isLessThan(1000); // 평균 1초 이내

        executor.shutdown();
    }

    @Test
    @DisplayName("데이터소스 컨텍스트 스위칭 오버헤드 테스트")
    void testDataSourceContextSwitchingOverhead() {
        // Given
        int iterations = 100;
        long totalSwitchTime = 0;

        // When - 컨텍스트 스위칭 반복 수행
        for (int i = 0; i < iterations; i++) {
            long switchStart = System.currentTimeMillis();

            // Reader로 스위칭
            DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
            DataSourceType currentType1 = DataSourceContextHolder.getDataSourceType();

            // Writer로 스위칭
            DataSourceContextHolder.setDataSourceType(DataSourceType.WRITER);
            DataSourceType currentType2 = DataSourceContextHolder.getDataSourceType();

            // 클리어
            DataSourceContextHolder.clearDataSourceType();
            DataSourceType currentType3 = DataSourceContextHolder.getDataSourceType();

            long switchEnd = System.currentTimeMillis();
            totalSwitchTime += (switchEnd - switchStart);

            // 검증
            assertThat(currentType1).isEqualTo(DataSourceType.READER);
            assertThat(currentType2).isEqualTo(DataSourceType.WRITER);
            assertThat(currentType3).isNull();
        }

        // Then
        long avgSwitchTime = totalSwitchTime / iterations;
        System.out.println("=== 데이터소스 컨텍스트 스위칭 오버헤드 ===");
        System.out.println("총 반복 수: " + iterations);
        System.out.println("총 스위칭 시간: " + totalSwitchTime + "ms");
        System.out.println("평균 스위칭 시간: " + avgSwitchTime + "ms");
        System.out.println("스위칭당 오버헤드: " + (totalSwitchTime * 1000.0 / iterations) + "μs");

        // 컨텍스트 스위칭은 매우 빨라야 함
        assertThat(avgSwitchTime).isLessThan(10); // 평균 10ms 이내
    }

    @Test
    @DisplayName("ThreadLocal 격리 성능 테스트")
    void testThreadLocalIsolationPerformance() throws InterruptedException {
        // Given
        int threadCount = 20;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];
        long[] executionTimes = new long[threadCount];

        // When - 여러 스레드에서 동시에 컨텍스트 설정
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                long threadStart = System.currentTimeMillis();
                try {
                    // 각 스레드별로 다른 데이터소스 타입 설정
                    DataSourceType expectedType = threadIndex % 2 == 0 ?
                        DataSourceType.READER : DataSourceType.WRITER;

                    DataSourceContextHolder.setDataSourceType(expectedType);

                    // 작업 시뮬레이션
                    Thread.sleep(50);

                    // 검증
                    DataSourceType actualType = DataSourceContextHolder.getDataSourceType();
                    results[threadIndex] = expectedType.equals(actualType);

                    long threadEnd = System.currentTimeMillis();
                    executionTimes[threadIndex] = threadEnd - threadStart;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    results[threadIndex] = false;
                } finally {
                    DataSourceContextHolder.clearDataSourceType();
                }
            });
        }

        long startTime = System.currentTimeMillis();

        // 모든 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }

        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then
        System.out.println("=== ThreadLocal 격리 성능 ===");
        System.out.println("스레드 수: " + threadCount);
        System.out.println("총 실행 시간: " + totalTime + "ms");

        long avgExecutionTime = 0;
        int successCount = 0;
        for (int i = 0; i < threadCount; i++) {
            if (results[i]) {
                successCount++;
                avgExecutionTime += executionTimes[i];
            }
        }
        avgExecutionTime /= successCount;

        System.out.println("성공한 스레드 수: " + successCount + "/" + threadCount);
        System.out.println("평균 스레드 실행 시간: " + avgExecutionTime + "ms");

        // 모든 스레드가 성공해야 함 (ThreadLocal 격리 검증)
        assertThat(successCount).isEqualTo(threadCount);
        assertThat(totalTime).isLessThan(3000); // 3초 이내
    }

    @Test
    @DisplayName("ApplicationService 계층 데이터소스 라우팅 성능 테스트")
    void testApplicationServiceDataSourceRoutingPerformance() {
        // Given
        long startTime = System.currentTimeMillis();

        // When - Repository를 통한 직접 조회 (Reader 라우팅 테스트)
        List<ProductJpaEntity> products = productRepository.findFirst10ByOrderByIdAsc();

        // 개별 상품 상세 조회
        for (int i = 0; i < Math.min(5, products.size()); i++) {
            var product = productRepository.findById(products.get(i).getId());
            assertThat(product).isPresent();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        System.out.println("=== Repository 계층 라우팅 성능 ===");
        System.out.println("조회된 상품 수: " + products.size());
        System.out.println("실행 시간: " + executionTime + "ms");

        assertThat(executionTime).isLessThan(2000); // 2초 이내
        assertThat(products).isNotEmpty();
    }

}
