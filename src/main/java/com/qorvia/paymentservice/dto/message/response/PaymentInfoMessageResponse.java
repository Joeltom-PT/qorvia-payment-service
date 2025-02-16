package com.qorvia.paymentservice.dto.message.response;

import lombok.Data;

@Data
public class PaymentInfoMessageResponse {
    private String paymentUrl;
    private String sessionId;
    private String tempSessionId;
}
