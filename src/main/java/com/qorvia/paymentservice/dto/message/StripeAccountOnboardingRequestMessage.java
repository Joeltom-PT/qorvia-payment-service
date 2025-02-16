package com.qorvia.paymentservice.dto.message;

import lombok.Data;

@Data
public class StripeAccountOnboardingRequestMessage {
    private String type = "stipe-onboarding-message-request";
    private String email;
    private Long organizerId;
}