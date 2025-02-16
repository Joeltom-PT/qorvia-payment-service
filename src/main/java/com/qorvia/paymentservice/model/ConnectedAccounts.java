package com.qorvia.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "connected_accounts")
public class ConnectedAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long organizerId;
    private String organizerEmail;
    private String organizerAccountId;
    @Enumerated(value = EnumType.STRING)
    private AccountStatus accountStatus;
}
