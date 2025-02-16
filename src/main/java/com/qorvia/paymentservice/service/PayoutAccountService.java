package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.PaymentSessionInfo;
import com.qorvia.paymentservice.dto.RefundDTO;
import com.qorvia.paymentservice.dto.message.StripeAccountOnboardingRequestMessage;
import com.qorvia.paymentservice.dto.request.PaymentRequestDTO;
import com.qorvia.paymentservice.dto.request.RefundRequestDTO;
import com.qorvia.paymentservice.model.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Refund;
import org.springframework.http.ResponseEntity;

public interface PayoutAccountService {

    String connectOrganizerAccount(StripeAccountOnboardingRequestMessage stripeAccountOnboardingRequest) throws StripeException;

    void handleAccountUpdate(Account account);

    ResponseEntity<?> getStripeAccountDetails(Long organizerId);

    boolean isConnected(Long organizerId);

    ResponseEntity<?> removeConnectedAccount(Long organizerId);

}
