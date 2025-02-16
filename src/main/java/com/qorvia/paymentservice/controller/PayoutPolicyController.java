package com.qorvia.paymentservice.controller;

import com.qorvia.paymentservice.dto.PayoutPolicyDTO;
import com.qorvia.paymentservice.service.PayoutPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payout-policy")
@RequiredArgsConstructor
public class PayoutPolicyController {

    private final PayoutPolicyService payoutPolicyService;

    @GetMapping("/get")
    public ResponseEntity<?> getPayoutPolicy() {
        try {
            PayoutPolicyDTO policyDTO = payoutPolicyService.getPayoutPolicy();
            return ResponseEntity.ok(policyDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the payout policy.");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updatePayoutPolicy(@RequestBody PayoutPolicyDTO payoutPolicyDTO){
        return payoutPolicyService.updatePayoutPolicy(payoutPolicyDTO);
    }


}
