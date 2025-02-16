package com.qorvia.paymentservice.repository;

import com.qorvia.paymentservice.model.ConnectedAccounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectedAccountsRepository extends JpaRepository<ConnectedAccounts, Long> {
    ConnectedAccounts findByOrganizerAccountId(String id);

    Optional<ConnectedAccounts> findByOrganizerId(Long organizerId);

    boolean existsByOrganizerId(Long organizerId);
}
