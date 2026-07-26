package com.msa.commerce.payment.application.port.in;

import com.msa.commerce.payment.application.port.in.command.RefundPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

public interface RefundPaymentUseCase {

    PaymentResponse refund(RefundPaymentCommand command);

}
