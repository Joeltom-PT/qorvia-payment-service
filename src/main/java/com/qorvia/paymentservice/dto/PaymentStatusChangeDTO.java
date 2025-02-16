package com.qorvia.paymentservice.dto;

import com.qorvia.paymentservice.model.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusChangeDTO {
    private String paymentSessionId;
    private String userEmail;
    private String eventId;
    private PaymentStatus paymentStatus;
}
