package com.msa.commerce.payment.adapter.out.gateway;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.gateway")
public record PaymentGatewayProperties(String providerName, Mock mock, Retry retry) {

    private static final String DEFAULT_PROVIDER_NAME = "MOCK_PG";

    public PaymentGatewayProperties {
        providerName = providerName != null ? providerName : DEFAULT_PROVIDER_NAME;
        mock = mock != null ? mock : new Mock(null);
        retry = retry != null ? retry : new Retry(null, null, null);
    }

    public record Mock(BigDecimal approvalLimit) {

        private static final BigDecimal DEFAULT_APPROVAL_LIMIT = new BigDecimal("10000000.0000");

        public Mock {
            approvalLimit = approvalLimit != null ? approvalLimit : DEFAULT_APPROVAL_LIMIT;
        }

    }

    public record Retry(Integer maxAttempts, Long initialDelayMillis, Double multiplier) {

        private static final int DEFAULT_MAX_ATTEMPTS = 3;

        private static final long DEFAULT_INITIAL_DELAY_MILLIS = 200L;

        private static final double DEFAULT_MULTIPLIER = 2.0;

        public Retry {
            maxAttempts = maxAttempts != null && maxAttempts > 0 ? maxAttempts : DEFAULT_MAX_ATTEMPTS;
            initialDelayMillis = initialDelayMillis != null && initialDelayMillis >= 0
                ? initialDelayMillis
                : DEFAULT_INITIAL_DELAY_MILLIS;
            multiplier = multiplier != null && multiplier >= 1.0 ? multiplier : DEFAULT_MULTIPLIER;
        }

    }

}
