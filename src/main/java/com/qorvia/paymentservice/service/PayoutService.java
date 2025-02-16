package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.request.GetAllPayoutRequest;
import com.qorvia.paymentservice.model.TransactionStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import org.springframework.http.ResponseEntity;

public interface PayoutService {

    void updatePayout(Long eventOrganizerId, long organizerCreditedAmount, String eventId, TransactionStatus transactionStatus);

    ResponseEntity<?> getAllPayouts(GetAllPayoutRequest getAllPayoutRequest);

    ResponseEntity<?> performPayout(Long payoutId);
}
