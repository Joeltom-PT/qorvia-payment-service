package com.qorvia.paymentservice.dto;

import com.qorvia.paymentservice.model.AccountStatus;
import lombok.Data;

@Data
public class AccountDTO {
    private String organizerEmail;
    private String organizerAccountId;
    private AccountStatus accountStatus;
}
