package com.msa.commerce.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.payment.adapter.in.web.PaymentCommandController;
import com.msa.commerce.payment.adapter.in.web.PaymentQueryController;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentRepository;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PaymentServiceApplication 컨텍스트 로딩 테스트")
class PaymentServiceApplicationTest {

    @Autowired
    private PaymentCommandController paymentCommandController;

    @Autowired
    private PaymentQueryController paymentQueryController;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentGatewayPort paymentGatewayPort;

    @Test
    @DisplayName("애플리케이션 컨텍스트가 기동되고 어댑터가 모두 주입된다")
    void contextLoads() {
        assertThat(paymentCommandController).isNotNull();
        assertThat(paymentQueryController).isNotNull();
        assertThat(paymentRepository).isNotNull();
        assertThat(paymentGatewayPort).isNotNull();
    }

    @Test
    @DisplayName("PG 설정 프로퍼티가 바인딩된다")
    void gatewayPropertiesAreBound() {
        assertThat(paymentGatewayPort.providerName()).isEqualTo("TEST_PG");
    }

}
