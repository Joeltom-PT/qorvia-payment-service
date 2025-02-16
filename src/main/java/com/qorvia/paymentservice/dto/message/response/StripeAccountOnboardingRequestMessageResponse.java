package com.qorvia.paymentservice.dto.message.response;

import lombok.Data;

@Data
public class StripeAccountOnboardingRequestMessageResponse {
    private String url;
    private String message;
}
