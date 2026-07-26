package com.msa.commerce.materializedview.adapter.out.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.materializedview.application.port.out.OrderViewRepository;
import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

import lombok.RequiredArgsConstructor;

// 모든 갱신은 DB 원자적 upsert(ON DUPLICATE KEY)로 수행해 동시 소비 시 레이스를 제거한다.
// LIFETIME 행은 unique key의 NULL 중복 문제를 피하기 위해 sentinel 날짜(1970-01-01)를 사용한다.
@Repository
@RequiredArgsConstructor
public class OrderViewRepositoryAdapter implements OrderViewRepository {

    private static final String UPSERT_PRODUCT_SALES = """
        INSERT INTO product_sales_summary
            (product_id, product_sku, product_name, total_quantity_sold, total_revenue,
             average_selling_price, total_orders, last_sale_date, first_sale_date,
             summary_period, period_start_date)
        VALUES (?, '', ?, ?, ?, ?, 1, ?, ?, 'LIFETIME', '1970-01-01') AS new
        ON DUPLICATE KEY UPDATE
            product_name = new.product_name,
            total_quantity_sold = product_sales_summary.total_quantity_sold + new.total_quantity_sold,
            total_revenue = product_sales_summary.total_revenue + new.total_revenue,
            total_orders = product_sales_summary.total_orders + 1,
            average_selling_price = product_sales_summary.total_revenue
                / NULLIF(product_sales_summary.total_quantity_sold, 0),
            last_sale_date = GREATEST(product_sales_summary.last_sale_date, new.last_sale_date),
            first_sale_date = LEAST(product_sales_summary.first_sale_date, new.first_sale_date)
        """;

    private static final String UPSERT_USER_ORDER_CREATED = """
        INSERT INTO user_order_summary
            (user_id, total_orders, pending_orders, total_spent, average_order_value,
             lifetime_value, first_order_date, last_order_date, summary_period, period_start_date)
        VALUES (?, 1, 1, ?, ?, ?, ?, ?, 'LIFETIME', '1970-01-01') AS new
        ON DUPLICATE KEY UPDATE
            total_orders = user_order_summary.total_orders + 1,
            pending_orders = user_order_summary.pending_orders + 1,
            total_spent = user_order_summary.total_spent + new.total_spent,
            lifetime_value = user_order_summary.lifetime_value + new.total_spent,
            average_order_value = user_order_summary.total_spent / user_order_summary.total_orders,
            first_order_date = LEAST(user_order_summary.first_order_date, new.first_order_date),
            last_order_date = GREATEST(user_order_summary.last_order_date, new.last_order_date)
        """;

    private static final String UPDATE_USER_ORDER_STATUS = """
        UPDATE user_order_summary
        SET pending_orders = GREATEST(CAST(pending_orders AS SIGNED) + ?, 0),
            completed_orders = GREATEST(CAST(completed_orders AS SIGNED) + ?, 0),
            cancelled_orders = GREATEST(CAST(cancelled_orders AS SIGNED) + ?, 0)
        WHERE user_id = ? AND summary_period = 'LIFETIME' AND period_start_date = '1970-01-01'
        """;

    private static final String UPSERT_DAILY_METRICS_CREATED = """
        INSERT INTO daily_business_metrics (business_date, total_orders, total_revenue, products_sold)
        VALUES (?, 1, ?, ?) AS new
        ON DUPLICATE KEY UPDATE
            total_orders = daily_business_metrics.total_orders + 1,
            total_revenue = daily_business_metrics.total_revenue + new.total_revenue,
            products_sold = daily_business_metrics.products_sold + new.products_sold,
            average_order_value = daily_business_metrics.total_revenue / daily_business_metrics.total_orders
        """;

    private static final String UPSERT_DAILY_METRICS_STATUS = """
        INSERT INTO daily_business_metrics (business_date, completed_orders, cancelled_orders)
        VALUES (?, GREATEST(?, 0), GREATEST(?, 0))
        ON DUPLICATE KEY UPDATE
            completed_orders = GREATEST(CAST(completed_orders AS SIGNED) + ?, 0),
            cancelled_orders = GREATEST(CAST(cancelled_orders AS SIGNED) + ?, 0)
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void applyOrderCreated(OrderCreatedView view) {
        LocalDate orderDate = view.orderDate().toLocalDate();
        view.orderItems().forEach(item -> upsertProductSales(item, orderDate));
        upsertUserOrderCreated(view, orderDate);
        jdbcTemplate.update(UPSERT_DAILY_METRICS_CREATED, orderDate, view.totalAmount(), view.totalQuantity());
    }

    @Override
    @Transactional
    public void applyStatusChanged(OrderStatusChangedView view) {
        if (!view.hasCategoryChanged()) {
            return;
        }
        jdbcTemplate.update(UPDATE_USER_ORDER_STATUS,
            view.pendingDelta(), view.completedDelta(), view.cancelledDelta(), view.customerId());
        jdbcTemplate.update(UPSERT_DAILY_METRICS_STATUS,
            view.statusChangedAt().toLocalDate(), view.completedDelta(), view.cancelledDelta(),
            view.completedDelta(), view.cancelledDelta());
    }

    private void upsertProductSales(OrderCreatedView.OrderItemView item, LocalDate orderDate) {
        jdbcTemplate.update(UPSERT_PRODUCT_SALES,
            item.productId(), item.productName(), item.quantity(), item.totalPrice(),
            averagePrice(item), orderDate, orderDate);
    }

    private void upsertUserOrderCreated(OrderCreatedView view, LocalDate orderDate) {
        jdbcTemplate.update(UPSERT_USER_ORDER_CREATED,
            view.customerId(), view.totalAmount(), view.totalAmount(), view.totalAmount(),
            orderDate, orderDate);
    }

    private BigDecimal averagePrice(OrderCreatedView.OrderItemView item) {
        if (item.quantity() == null || item.quantity() == 0) {
            return BigDecimal.ZERO;
        }
        return item.totalPrice().divide(BigDecimal.valueOf(item.quantity()), 2, RoundingMode.HALF_UP);
    }

}
