package com.qorvia.paymentservice.repository;

import com.qorvia.paymentservice.model.Payout;
import com.qorvia.paymentservice.model.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long>, JpaSpecificationExecutor<Payout> {
    Optional<Payout> findByOrganizerId(Long eventOrganizerId);

    Optional<Payout> findFirstByOrganizerIdAndPayoutStatus(Long eventOrganizerId, PayoutStatus payoutStatus);
}
