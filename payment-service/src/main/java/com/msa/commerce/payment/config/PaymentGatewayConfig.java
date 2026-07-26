package com.msa.commerce.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.msa.commerce.payment.adapter.out.gateway.MockPaymentGatewayAdapter;
import com.msa.commerce.payment.adapter.out.gateway.PaymentGatewayProperties;
import com.msa.commerce.payment.adapter.out.gateway.RetryablePaymentGatewayAdapter;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;

@Configuration
public class PaymentGatewayConfig {

    // 실제 PG 어댑터로 교체할 때 delegate 파라미터 타입만 바꾸면 된다
    @Bean
    @Primary
    public PaymentGatewayPort paymentGatewayPort(MockPaymentGatewayAdapter delegate,
        PaymentGatewayProperties properties) {
        return new RetryablePaymentGatewayAdapter(delegate, properties.retry());
    }

}
