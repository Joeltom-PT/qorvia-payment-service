package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.PayoutPolicyDTO;
import org.springframework.http.ResponseEntity;

public interface PayoutPolicyService {
    ResponseEntity<?> updatePayoutPolicy(PayoutPolicyDTO payoutPolicyDTO);

    PayoutPolicyDTO getPayoutPolicy();
}
