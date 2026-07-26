package com.msa.commerce.payment.adapter.in.web.mapper;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.payment.adapter.in.web.dto.request.ProcessPaymentRequest;
import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.domain.PaymentMethod;

@DisplayName("PaymentCommandMapper 테스트")
class PaymentCommandMapperTest {

    private final PaymentCommandMapper mapper = new PaymentCommandMapper();

    @Test
    @DisplayName("요청의 모든 필드를 커맨드로 옮긴다")
    void mapsAllFields() {
        UUID orderId = UUID.randomUUID();
        ProcessPaymentRequest request = new ProcessPaymentRequest(orderId, 1001L,
            new BigDecimal("15000.0000"), "USD", PaymentMethod.DIGITAL_WALLET, Map.of("wallet", "TOSS"));

        ProcessPaymentCommand command = mapper.toProcessPaymentCommand(request, null);

        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.customerId()).isEqualTo(1001L);
        assertThat(command.amount()).isEqualByComparingTo("15000.0000");
        assertThat(command.currency()).isEqualTo("USD");
        assertThat(command.paymentMethod()).isEqualTo(PaymentMethod.DIGITAL_WALLET);
        assertThat(command.paymentDetails()).containsEntry("wallet", "TOSS");
    }

    @Test
    @DisplayName("통화가 없으면 KRW 로 채운다")
    void defaultsCurrency() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(UUID.randomUUID(), 1001L,
            new BigDecimal("15000.0000"), null, PaymentMethod.CREDIT_CARD, null);

        assertThat(mapper.toProcessPaymentCommand(request, null).currency()).isEqualTo("KRW");
    }

}
