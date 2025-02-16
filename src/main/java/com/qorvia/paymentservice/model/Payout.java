package com.qorvia.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long organizerId;

    private Long amount;

    private LocalDateTime payoutDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PayoutStatus payoutStatus;
}
