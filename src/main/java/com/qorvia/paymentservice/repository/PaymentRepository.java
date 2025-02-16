package com.qorvia.paymentservice.repository;

import com.qorvia.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findBySessionId(String sessionId);

    Payment findByRefundId(String id);
}
