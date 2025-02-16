package com.qorvia.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    private Long eventOrganizerId;
    private String userEmail;
    private Long amount;
    private String currency;
    private String eventName;
    private String sessionId;
    private String refundId;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private Long expirationTimestamp;

}
