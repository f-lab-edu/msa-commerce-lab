package com.msa.commerce.payment.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private static final Set<PaymentStatus> ACTIVE_STATUSES = PaymentStatus.activeStatuses();

    private final PaymentJpaRepository paymentJpaRepository;

    private final PaymentDomainMapper paymentDomainMapper;

    // 조회 후 갱신까지가 한 트랜잭션이어야 낙관적 락 버전이 올바르게 증가한다.
    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = paymentJpaRepository.findByPaymentId(payment.getPaymentId())
            .map(existing -> {
                existing.updateFrom(payment);
                return existing;
            })
            .orElseGet(() -> PaymentJpaEntity.from(payment));

        return paymentDomainMapper.toDomain(paymentJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByPaymentId(UUID paymentId) {
        return paymentJpaRepository.findByPaymentId(paymentId)
            .map(paymentDomainMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
            .stream()
            .map(paymentDomainMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findActiveByOrderId(UUID orderId) {
        return paymentJpaRepository.findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(orderId, ACTIVE_STATUSES)
            .map(paymentDomainMapper::toDomain);
    }

}
