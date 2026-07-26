package com.msa.commerce.payment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.payment.application.port.out.PaymentEventPublisher;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.domain.Payment;

import lombok.RequiredArgsConstructor;

/*
 * 결제 상태 저장과 outbox 적재를 하나의 트랜잭션으로 묶는 지점.
 * 유스케이스 서비스는 PG 호출 때문에 트랜잭션을 걸 수 없어서, 커밋이 필요한 구간만
 * 별도 빈으로 뺐다. (같은 빈 안에서 @Transactional 메서드를 self-invocation 하면
 * 프록시를 타지 않아 트랜잭션이 걸리지 않는다.)
 */
@Service
@RequiredArgsConstructor
public class PaymentResultRecorder {

    private final PaymentRepository paymentRepository;

    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public Payment recordAndPublish(Payment payment, String correlationId) {
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentResult(saved, correlationId);
        return saved;
    }

    // 이벤트 발행 없이 상태만 저장한다 (환불처럼 payment.result 를 내보내지 않는 경우)
    @Transactional
    public Payment recordOnly(Payment payment) {
        return paymentRepository.save(payment);
    }

}
