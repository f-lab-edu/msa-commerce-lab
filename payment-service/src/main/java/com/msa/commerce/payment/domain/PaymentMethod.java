package com.msa.commerce.payment.domain;

public enum PaymentMethod {

    CREDIT_CARD,

    DEBIT_CARD,

    BANK_TRANSFER,

    VIRTUAL_ACCOUNT,

    DIGITAL_WALLET,

    CRYPTOCURRENCY,

    POINT,

    GIFT_CARD;

    // 가상계좌는 입금 대기 상태가 존재하므로 승인 즉시 매입되지 않는다
    public boolean requiresDeferredSettlement() {
        return this == VIRTUAL_ACCOUNT || this == BANK_TRANSFER;
    }

}
