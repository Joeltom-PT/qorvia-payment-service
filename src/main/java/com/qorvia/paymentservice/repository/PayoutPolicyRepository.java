package com.qorvia.paymentservice.repository;

import com.qorvia.paymentservice.model.PayoutPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutPolicyRepository extends JpaRepository<PayoutPolicy, Long> {
}
