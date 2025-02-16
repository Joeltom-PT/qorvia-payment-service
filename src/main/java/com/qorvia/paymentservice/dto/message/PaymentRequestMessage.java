package com.qorvia.paymentservice.dto.message;

import lombok.Data;

@Data
public class PaymentRequestMessage {
    private String type = "payment-request-message";
    private String currency;
    private int amount;
    private String email;
    private String eventId;
    private Long eventOrganizerId;
    private String eventName;
    private String imgUrl;

}
