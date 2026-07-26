package com.msa.commerce.payment.adapter.out.gateway;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.gateway")
public record PaymentGatewayProperties(String providerName, Mock mock) {

    private static final String DEFAULT_PROVIDER_NAME = "MOCK_PG";

    public PaymentGatewayProperties {
        providerName = providerName != null ? providerName : DEFAULT_PROVIDER_NAME;
        mock = mock != null ? mock : new Mock(null);
    }

    public record Mock(BigDecimal approvalLimit) {

        private static final BigDecimal DEFAULT_APPROVAL_LIMIT = new BigDecimal("10000000.0000");

        public Mock {
            approvalLimit = approvalLimit != null ? approvalLimit : DEFAULT_APPROVAL_LIMIT;
        }

    }

}
