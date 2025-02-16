package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.PayoutPolicyDTO;
import com.qorvia.paymentservice.model.PayoutPolicy;
import com.qorvia.paymentservice.repository.PayoutPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PayoutPolicyServiceImpl implements PayoutPolicyService {

    private final PayoutPolicyRepository payoutPolicyRepository;

    @Override
    @Transactional
    public ResponseEntity<?> updatePayoutPolicy(PayoutPolicyDTO payoutPolicyDTO) {
        PayoutPolicy existingPolicy = payoutPolicyRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Payout policy not found"));

        existingPolicy.setAdminRevenuePercentage(payoutPolicyDTO.getAdminRevenuePercentage());
        existingPolicy.setPolicyDescription(payoutPolicyDTO.getPolicyDescription());
        existingPolicy.setNotificationMessage(payoutPolicyDTO.getNotificationMessage());
        existingPolicy.setUpdatedAt(LocalDateTime.now());

        payoutPolicyRepository.save(existingPolicy);

        return ResponseEntity.ok("Payout policy updated successfully");
    }

    @Override
    public PayoutPolicyDTO getPayoutPolicy() {
        PayoutPolicy existingPolicy = payoutPolicyRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Payout policy not found"));

        PayoutPolicyDTO payoutPolicyDTO = new PayoutPolicyDTO();
        payoutPolicyDTO.setAdminRevenuePercentage(existingPolicy.getAdminRevenuePercentage());
        payoutPolicyDTO.setPolicyDescription(existingPolicy.getPolicyDescription());
        payoutPolicyDTO.setCreatedAt(existingPolicy.getCreatedAt());
        payoutPolicyDTO.setUpdatedAt(existingPolicy.getUpdatedAt());
        payoutPolicyDTO.setNotificationMessage(existingPolicy.getNotificationMessage());

        return payoutPolicyDTO;
    }
}
