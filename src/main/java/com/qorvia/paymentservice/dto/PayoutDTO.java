package com.qorvia.paymentservice.dto;

import com.qorvia.paymentservice.model.PayoutStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayoutDTO {
    private Long id;

    private Long organizerId;

    private Long amount;

    private LocalDateTime payoutDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String transactionId;

    private PayoutStatus payoutStatus;
}
