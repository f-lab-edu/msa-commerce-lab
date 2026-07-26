package com.msa.commerce.materializedview.adapter.out.persistence;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderViewRepositoryAdapter 단위 테스트")
class OrderViewRepositoryAdapterTest {

    private static final LocalDateTime ORDER_DATE = LocalDateTime.of(2026, 7, 27, 10, 0);

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OrderViewRepositoryAdapter adapter;

    @Test
    @DisplayName("주문 생성 시 상품별 판매 집계와 사용자/일별 요약을 갱신한다")
    void appliesOrderCreated() {
        adapter.applyOrderCreated(createdView(2));

        verify(jdbcTemplate).update(contains("INSERT INTO product_sales_summary"),
            eq(101L), eq("무선 키보드"), eq(2), eq(new BigDecimal("50000")),
            eq(new BigDecimal("25000.00")), eq(LocalDate.of(2026, 7, 27)), eq(LocalDate.of(2026, 7, 27)));
        verify(jdbcTemplate).update(contains("INSERT INTO user_order_summary"),
            eq(7L), any(), any(), any(), any(), any());
        verify(jdbcTemplate).update(contains("INSERT INTO daily_business_metrics"),
            eq(LocalDate.of(2026, 7, 27)), eq(new BigDecimal("50000")), eq(2));
    }

    @Test
    @DisplayName("수량이 0인 항목은 평균 단가를 0으로 기록한다")
    void zeroQuantityAveragePriceFallsBackToZero() {
        adapter.applyOrderCreated(createdView(0));

        verify(jdbcTemplate).update(contains("INSERT INTO product_sales_summary"),
            eq(101L), eq("무선 키보드"), eq(0), eq(new BigDecimal("50000")),
            eq(BigDecimal.ZERO), any(), any());
    }

    @Test
    @DisplayName("카테고리가 바뀐 상태 변경은 카운터 갱신 쿼리를 수행한다")
    void appliesStatusChangeAcrossCategories() {
        adapter.applyStatusChanged(statusView("SHIPPED", "DELIVERED"));

        verify(jdbcTemplate).update(contains("UPDATE user_order_summary"),
            eq(-1), eq(1), eq(0), eq(7L));
        verify(jdbcTemplate).update(contains("INSERT INTO daily_business_metrics"),
            eq(LocalDate.of(2026, 7, 27)), eq(1), eq(0), eq(1), eq(0));
    }

    @Test
    @DisplayName("같은 카테고리 내 상태 변경은 어떤 쿼리도 수행하지 않는다")
    void skipsSameCategoryTransition() {
        adapter.applyStatusChanged(statusView("PENDING", "CONFIRMED"));

        verifyNoInteractions(jdbcTemplate);
    }

    private OrderCreatedView createdView(int quantity) {
        return new OrderCreatedView("event-1", UUID.randomUUID(), 7L,
            new BigDecimal("50000"), ORDER_DATE,
            List.of(new OrderCreatedView.OrderItemView(101L, "무선 키보드", quantity, new BigDecimal("50000"))));
    }

    private OrderStatusChangedView statusView(String previous, String current) {
        return new OrderStatusChangedView("event-2", UUID.randomUUID(), 7L, previous, current, ORDER_DATE);
    }

}
