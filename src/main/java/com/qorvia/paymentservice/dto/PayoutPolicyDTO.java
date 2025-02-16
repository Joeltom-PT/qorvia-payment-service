package com.qorvia.paymentservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayoutPolicyDTO {

    private BigDecimal adminRevenuePercentage;

    private String policyDescription;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String notificationMessage;
}
