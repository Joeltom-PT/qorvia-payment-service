package com.qorvia.paymentservice.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {


    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Bean
    public String configureStripe() {
        Stripe.apiKey = stripeSecretKey;
        return "Stripe API Key Configured";
    }

}
