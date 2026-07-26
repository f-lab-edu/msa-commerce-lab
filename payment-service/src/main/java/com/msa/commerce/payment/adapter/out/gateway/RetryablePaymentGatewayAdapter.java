package com.msa.commerce.payment.adapter.out.gateway;

import java.math.BigDecimal;
import java.util.function.Supplier;

import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayException;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentGatewayResult;
import com.msa.commerce.payment.domain.Payment;

import lombok.extern.slf4j.Slf4j;

/*
 * PG 통신 실패(PaymentGatewayException)만 지수 백오프로 재시도하는 데코레이터.
 * 거절(DECLINED)은 몇 번을 다시 물어봐도 답이 같으므로 재시도하지 않는다.
 */
@Slf4j
public class RetryablePaymentGatewayAdapter implements PaymentGatewayPort {

    private final PaymentGatewayPort delegate;

    private final int maxAttempts;

    private final long initialDelayMillis;

    private final double multiplier;

    public RetryablePaymentGatewayAdapter(PaymentGatewayPort delegate, PaymentGatewayProperties.Retry retry) {
        this.delegate = delegate;
        this.maxAttempts = retry.maxAttempts();
        this.initialDelayMillis = retry.initialDelayMillis();
        this.multiplier = retry.multiplier();
    }

    @Override
    public String providerName() {
        return delegate.providerName();
    }

    @Override
    public PaymentGatewayResult authorize(Payment payment) {
        return withRetry("authorize", payment, () -> delegate.authorize(payment));
    }

    @Override
    public PaymentGatewayActionResult cancel(Payment payment, String reason) {
        return withRetry("cancel", payment, () -> delegate.cancel(payment, reason));
    }

    @Override
    public PaymentGatewayActionResult refund(Payment payment, BigDecimal amount, String reason) {
        return withRetry("refund", payment, () -> delegate.refund(payment, amount, reason));
    }

    private <T> T withRetry(String operation, Payment payment, Supplier<T> call) {
        long delay = initialDelayMillis;
        PaymentGatewayException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return call.get();
            } catch (PaymentGatewayException e) {
                lastFailure = e;
                log.warn("Payment gateway {} failed: paymentId={}, attempt={}/{}, reason={}",
                    operation, payment.getPaymentId(), attempt, maxAttempts, e.getMessage());

                if (attempt < maxAttempts) {
                    sleep(delay);
                    delay = (long)(delay * multiplier);
                }
            }
        }

        throw new PaymentGatewayException(
            String.format("Payment gateway %s failed after %d attempts", operation, maxAttempts), lastFailure);
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException("Payment gateway retry was interrupted", e);
        }
    }

}
