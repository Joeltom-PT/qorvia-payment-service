package com.qorvia.paymentservice.repository;

import com.qorvia.paymentservice.model.AdminRevenue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRevenueRepository extends JpaRepository<AdminRevenue, Long> {
    Optional<AdminRevenue> findByEventId(String eventId);

    @Query("SELECT ar.eventId FROM AdminRevenue ar ORDER BY ar.adminRevenue DESC")
    List<String> findTop4EventsByRevenue(Pageable pageable);

}
