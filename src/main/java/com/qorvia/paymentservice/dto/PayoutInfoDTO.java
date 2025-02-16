package com.qorvia.paymentservice.dto;

import lombok.Data;

@Data
public class PayoutInfoDTO {
    private Long organizerId;
    private String country = "IN";
    private String currency = "usd";
    private String accountHolderName;
    private String accountHolderType = "individual";
    private String accountNumber;
    private String ifscCode;
    private long amount;
    private long commission;
}
