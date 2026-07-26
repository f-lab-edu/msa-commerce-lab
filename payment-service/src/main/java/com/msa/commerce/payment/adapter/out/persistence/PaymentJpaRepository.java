package com.msa.commerce.payment.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msa.commerce.payment.domain.PaymentStatus;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findByPaymentId(UUID paymentId);

    List<PaymentJpaEntity> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    Optional<PaymentJpaEntity> findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
        UUID orderId, Collection<PaymentStatus> statuses);

}
