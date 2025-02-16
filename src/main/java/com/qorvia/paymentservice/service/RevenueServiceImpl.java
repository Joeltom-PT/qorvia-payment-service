package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.PayoutPolicyDTO;
import com.qorvia.paymentservice.model.*;
import com.qorvia.paymentservice.repository.AdminRevenueRepository;
import com.qorvia.paymentservice.repository.OrganizerRevenueRepository;
import com.qorvia.paymentservice.service.PayoutPolicyService;
import com.qorvia.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueServiceImpl implements RevenueService {

    private final AdminRevenueRepository adminRevenueRepository;
    private final OrganizerRevenueRepository organizerRevenueRepository;
    private final PayoutPolicyService payoutPolicyService;
    private final PayoutService payoutService;
    private final PaymentService paymentService;

    @Override
    public void updateRevenue(String paymentSessionId) {
        Payment payment = paymentService.getPaymentBySessionId(paymentSessionId);

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            PayoutPolicyDTO payoutPolicy = payoutPolicyService.getPayoutPolicy();

            int adminRevenuePercentage = payoutPolicy.getAdminRevenuePercentage().intValue();
            if (adminRevenuePercentage < 0 || adminRevenuePercentage > 100) {
                log.error("Invalid admin revenue percentage: {}", adminRevenuePercentage);
                throw new IllegalArgumentException("Admin revenue percentage must be between 0 and 100.");
            }

            long adminRevenue = (payment.getAmount() * adminRevenuePercentage) / 100;

            if (adminRevenue > payment.getAmount()) {
                log.error("Calculated admin revenue exceeds total payment amount. Payment amount: {}, Admin revenue: {}", payment.getAmount(), adminRevenue);
                throw new IllegalStateException("Admin revenue cannot exceed the total payment amount.");
            }

            Optional<AdminRevenue> adminRevenueOpt = adminRevenueRepository.findByEventId(payment.getEventId());
            AdminRevenue adminRevenueToUpdate = adminRevenueOpt.orElse(new AdminRevenue());
            adminRevenueToUpdate.setEventId(payment.getEventId());
            adminRevenueToUpdate.setOrganizerId(payment.getEventOrganizerId());
            adminRevenueToUpdate.setAdminRevenue(adminRevenue);
            adminRevenueToUpdate.setStatus("PENDING");

            adminRevenueRepository.save(adminRevenueToUpdate);

            long organizerCreditedAmount = payment.getAmount() - adminRevenue;

            Optional<OrganizerRevenue> organizerRevenueOpt = organizerRevenueRepository.findByOrganizerId(payment.getEventOrganizerId());
            OrganizerRevenue organizerRevenueToUpdate = organizerRevenueOpt.orElse(new OrganizerRevenue());

            long totalTransferred = organizerRevenueToUpdate.getTotalTransferred() != null ? organizerRevenueToUpdate.getTotalTransferred() : 0L;
            long totalEarned = organizerRevenueToUpdate.getTotalEarned() != null ? organizerRevenueToUpdate.getTotalEarned() : 0L;

            organizerRevenueToUpdate.setOrganizerId(payment.getEventOrganizerId());
            organizerRevenueToUpdate.setPendingAmount(organizerCreditedAmount);
            organizerRevenueToUpdate.setTotalTransferred(totalTransferred);
            organizerRevenueToUpdate.setTotalEarned(totalEarned + organizerCreditedAmount);

            organizerRevenueRepository.save(organizerRevenueToUpdate);

            payoutService.updatePayout(payment.getEventOrganizerId(), organizerCreditedAmount, payment.getEventId(), TransactionStatus.CREDITED);

            log.info("Revenue updated for eventId: {}. Admin revenue: {}, Organizer credited amount: {}",
                    payment.getEventId(), adminRevenue, organizerCreditedAmount);
        } else {
            log.warn("Payment is not completed for sessionId: {}", paymentSessionId);
        }
    }



}
