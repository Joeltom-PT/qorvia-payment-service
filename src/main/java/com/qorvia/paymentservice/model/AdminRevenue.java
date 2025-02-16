package com.qorvia.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_revenue")
public class AdminRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private Long organizerId;

    private Long adminRevenue;

    private LocalDate createdAt;

    private LocalDate updatedAt;

    @Column(nullable = false)
    private String status;
}
