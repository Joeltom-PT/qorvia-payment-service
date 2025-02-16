package com.qorvia.paymentservice.repository;

import com.qorvia.paymentservice.model.OrganizerRevenue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerRevenueRepository extends JpaRepository<OrganizerRevenue,Long> {
    Optional<OrganizerRevenue> findByOrganizerId(Long eventOrganizerId);
}
