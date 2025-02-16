package com.qorvia.paymentservice.dto;

import com.qorvia.paymentservice.model.PaymentStatus;
import lombok.Data;

import lombok.Data;

@Data
public class RefundDTO {
    private String sessionId;
    private long refundAmount;
    private String refundStatus;
    private String errorMessage;
}
