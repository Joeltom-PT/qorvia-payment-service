package com.qorvia.paymentservice.config;

import com.qorvia.paymentservice.model.PayoutPolicy;
import com.qorvia.paymentservice.repository.PayoutPolicyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PayoutPolicyConfig {

    @Value("${payout.policy.notificationMessage}")
    private String notificationMessage;

    @Value("${payout.policy.policyDescription}")
    private String policyDescription;

    @Bean
    public CommandLineRunner commandLineRunner(PayoutPolicyRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                PayoutPolicy payoutPolicy = new PayoutPolicy();
                payoutPolicy.setAdminRevenuePercentage(new BigDecimal("15.00"));
                payoutPolicy.setPolicyDescription(policyDescription);
                payoutPolicy.setNotificationMessage(notificationMessage);
                repository.save(payoutPolicy);
            }
        };
    }

}
