package com.qorvia.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentInfoDTO {
    private String paymentUrl;
    private String sessionId;
    private String tempSessionId;
}
