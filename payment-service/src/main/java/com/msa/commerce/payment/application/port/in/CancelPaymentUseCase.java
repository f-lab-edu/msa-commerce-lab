package com.msa.commerce.payment.application.port.in;

import com.msa.commerce.payment.application.port.in.command.CancelPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

public interface CancelPaymentUseCase {

    PaymentResponse cancel(CancelPaymentCommand command);

}
