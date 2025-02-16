package com.qorvia.paymentservice.dto.message;

import com.qorvia.paymentservice.model.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusChangeMessage {
    private String type = "payment-status-change";
    private String paymentSessionId;
    private String userEmail;
    private String eventId;
    private PaymentStatus paymentStatus;
}
