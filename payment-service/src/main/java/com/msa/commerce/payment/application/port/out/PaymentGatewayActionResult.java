package com.msa.commerce.payment.application.port.out;

// 취소/환불처럼 승인/거절 두 갈래만 있는 PG 요청의 결과
public record PaymentGatewayActionResult(
    boolean approved,
    String gatewayTransactionId,
    String failureCode,
    String failureReason
) {

    public static PaymentGatewayActionResult approved(String gatewayTransactionId) {
        return new PaymentGatewayActionResult(true, gatewayTransactionId, null, null);
    }

    public static PaymentGatewayActionResult rejected(String failureCode, String failureReason) {
        return new PaymentGatewayActionResult(false, null, failureCode, failureReason);
    }

}
