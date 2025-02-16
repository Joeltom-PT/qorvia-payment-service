package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.PaymentSessionInfo;
import com.qorvia.paymentservice.dto.PayoutInfoDTO;
import com.qorvia.paymentservice.dto.RefundDTO;
import com.qorvia.paymentservice.dto.request.PaymentRequestDTO;
import com.qorvia.paymentservice.dto.request.RefundRequestDTO;
import com.qorvia.paymentservice.model.Payment;
import com.qorvia.paymentservice.model.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PaymentService {

    RefundDTO refundPayment(RefundRequestDTO refundRequest) throws StripeException;

    void updatePaymentStatus(String sessionId, PaymentStatus status);

    PaymentSessionInfo createCheckoutSession(PaymentRequestDTO paymentRequestDTO) throws StripeException;

    void handleRefundSucceeded(Refund refund);

    Payment getPaymentBySessionId(String sessionId);

}
