package com.qorvia.paymentservice.dto.request;

import com.qorvia.paymentservice.model.PayoutStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GetAllPayoutRequest {
    private PayoutStatus payoutStatus;
    private Long organizerId;
    private LocalDate startDate;
    private LocalDate endDate;
}
