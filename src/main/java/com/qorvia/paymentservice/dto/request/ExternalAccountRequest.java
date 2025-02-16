package com.qorvia.paymentservice.dto.request;

import lombok.Data;

@Data
public class ExternalAccountRequest {
    private String accountId;
    private String accountHolderName;
    private String accountHolderType;
    private String routingNumber;
    private String accountNumber;
}
