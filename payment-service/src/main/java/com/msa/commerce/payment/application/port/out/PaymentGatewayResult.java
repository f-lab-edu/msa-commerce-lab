package com.msa.commerce.payment.application.port.out;

public record PaymentGatewayResult(
    Outcome outcome,
    String externalPaymentId,
    String gatewayTransactionId,
    String approvalNumber,
    String failureCode,
    String failureReason
) {

    public static PaymentGatewayResult authorized(String externalPaymentId, String gatewayTransactionId,
        String approvalNumber) {
        return new PaymentGatewayResult(Outcome.AUTHORIZED, externalPaymentId, gatewayTransactionId, approvalNumber,
            null, null);
    }

    public static PaymentGatewayResult captured(String externalPaymentId, String gatewayTransactionId,
        String approvalNumber) {
        return new PaymentGatewayResult(Outcome.CAPTURED, externalPaymentId, gatewayTransactionId, approvalNumber,
            null, null);
    }

    public static PaymentGatewayResult declined(String failureCode, String failureReason) {
        return new PaymentGatewayResult(Outcome.DECLINED, null, null, null, failureCode, failureReason);
    }

    public boolean isApproved() {
        return outcome != Outcome.DECLINED;
    }

    public enum Outcome {

        // 승인만 완료 (매입은 별도) — 가상계좌/계좌이체처럼 정산이 지연되는 수단
        AUTHORIZED,

        // 승인과 매입이 한 번에 끝난 경우
        CAPTURED,

        // PG사가 거절 (한도 초과, 카드 오류 등) — 재시도해도 결과가 같다
        DECLINED
    }

}
