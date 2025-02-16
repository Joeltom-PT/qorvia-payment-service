package com.qorvia.paymentservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundRequestDTO {
    private String sessionId;
    private int refundPercentage;
}
