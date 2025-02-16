package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.dto.AccountDTO;
import com.qorvia.paymentservice.dto.PayoutDTO;
import com.qorvia.paymentservice.dto.request.GetAllPayoutRequest;
import com.qorvia.paymentservice.model.*;
import com.qorvia.paymentservice.repository.ConnectedAccountsRepository;
import com.qorvia.paymentservice.repository.PayoutRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Charge;
import com.stripe.model.Transfer;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.TransferCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutServiceImpl implements PayoutService {

    private final PayoutRepository payoutRepository;
    private final ConnectedAccountsRepository connectedAccountsRepository;


    @Override
    public void updatePayout(Long eventOrganizerId, long organizerCreditedAmount, String eventId, TransactionStatus transactionStatus) {
        Payout payout = payoutRepository.findFirstByOrganizerIdAndPayoutStatus(eventOrganizerId, PayoutStatus.PENDING)
                .orElseGet(() -> {
                    Payout newPayout = new Payout();
                    newPayout.setOrganizerId(eventOrganizerId);
                    newPayout.setCreatedAt(LocalDateTime.now());
                    return newPayout;
                });

        if (transactionStatus == TransactionStatus.CREDITED) {
            payout.setAmount(payout.getAmount() != null ? payout.getAmount() + organizerCreditedAmount : organizerCreditedAmount);
            payout.setUpdatedAt(LocalDateTime.now());

            log.info("Payout credited (pending) for organizerId: {}, amount: {}", eventOrganizerId, organizerCreditedAmount);
        } else if (transactionStatus == TransactionStatus.DEBITED) {
            if (payout.getAmount() == null || payout.getAmount() < organizerCreditedAmount) {
                throw new IllegalArgumentException("Insufficient funds to debit the amount");
            }

            payout.setAmount(payout.getAmount() - organizerCreditedAmount);
            payout.setUpdatedAt(LocalDateTime.now());

            log.info("Payout debited (pending) for organizerId: {}, amount: {}", eventOrganizerId, organizerCreditedAmount);
        } else {
            throw new IllegalArgumentException("Invalid transaction status");
        }

        payout.setPayoutDate(null);
        payout.setTransactionId(null);
        payout.setPayoutStatus(PayoutStatus.PENDING);

        payoutRepository.save(payout);
        log.info("Payout record updated for organizerId: {}", eventOrganizerId);
    }

    @Override
    public ResponseEntity<?> getAllPayouts(GetAllPayoutRequest getAllPayoutRequest) {
        Specification<Payout> spec = Specification.where(null);

        if (getAllPayoutRequest.getPayoutStatus() != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("payoutStatus"), getAllPayoutRequest.getPayoutStatus()));
        }


        if (getAllPayoutRequest.getOrganizerId() != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("organizerId"), getAllPayoutRequest.getOrganizerId()));
        }

        if (getAllPayoutRequest.getStartDate() != null && getAllPayoutRequest.getEndDate() != null) {
            spec = spec.and((root, query, builder) ->
                    builder.between(root.get("payoutDate"), getAllPayoutRequest.getStartDate().atStartOfDay(),
                            getAllPayoutRequest.getEndDate().atTime(23, 59, 59)));
        }

        if (getAllPayoutRequest.getStartDate() != null && getAllPayoutRequest.getEndDate() == null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("payoutDate"), getAllPayoutRequest.getStartDate().atStartOfDay()));
        }

        if (getAllPayoutRequest.getEndDate() != null && getAllPayoutRequest.getStartDate() == null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("payoutDate"), getAllPayoutRequest.getEndDate().atTime(23, 59, 59)));
        }

        List<Payout> payouts = payoutRepository.findAll(spec);

        List<PayoutDTO> payoutDTOs = payouts.stream()
                .map(payout -> {
                    PayoutDTO payoutDTO = new PayoutDTO();
                    payoutDTO.setId(payout.getId());
                    payoutDTO.setOrganizerId(payout.getOrganizerId());
                    payoutDTO.setAmount(payout.getAmount());
                    payoutDTO.setPayoutDate(payout.getPayoutDate());
                    payoutDTO.setCreatedAt(payout.getCreatedAt());
                    payoutDTO.setUpdatedAt(payout.getUpdatedAt());
                    payoutDTO.setTransactionId(payout.getTransactionId());
                    payoutDTO.setPayoutStatus(payout.getPayoutStatus());
                    return payoutDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(payoutDTOs);
    }

    @Override
    @Transactional
    public ResponseEntity<?> performPayout(Long payoutId) {
        Optional<Payout> optionalPayout = payoutRepository.findById(payoutId);

        if (optionalPayout.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payout with id " + payoutId + " not found.");
        }

        Payout payout = optionalPayout.get();

        if (payout.getPayoutStatus() == PayoutStatus.PAID) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Payout already completed.");
        }

        try {
            String transactionId = stripeTransferHandler(payout.getOrganizerId(), payout.getAmount());

            payout.setTransactionId(transactionId);
            payout.setPayoutStatus(PayoutStatus.PAID);
            LocalDateTime payoutDate = LocalDateTime.now();
            payout.setUpdatedAt(payoutDate);
            payout.setPayoutDate(payoutDate);

            payoutRepository.save(payout);

            return ResponseEntity.status(HttpStatus.OK).body("Payout completed successfully.");
        } catch (Exception e) {
            log.error("Error processing payout with ID {}: {}", payoutId, e.getMessage(), e);

            payout.setPayoutStatus(PayoutStatus.FAILED);
            payout.setUpdatedAt(LocalDateTime.now());

            payoutRepository.save(payout);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the payout: " + e.getMessage());
        }
    }

    private String stripeTransferHandler(long organizerId, long amount) throws Exception {
        Optional<ConnectedAccounts> account = connectedAccountsRepository.findByOrganizerId(organizerId);

        if (account.isEmpty()) {
            throw new Exception("Connected account for organizer ID " + organizerId + " not found.");
        }

        if (isDevelopmentEnvironment()) {
            return "tr_" + UUID.randomUUID().toString();
        }

        Transfer transfer = createTransfer(amount, account.get().getOrganizerAccountId());
        return transfer.getId();
    }

    private Transfer createTransfer(Long amount, String connectedAccountId) throws Exception {
        log.info("Paying {} to the account : {}", amount , connectedAccountId);
        try {
            TransferCreateParams transferParams = TransferCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("inr")
                    .setDestination(connectedAccountId)
                    .build();

            return Transfer.create(transferParams);
        } catch (Exception e) {
            throw new Exception("Error creating transfer: " + e.getMessage(), e);
        }
    }

    private boolean isDevelopmentEnvironment() {
        return true;
    }


}
