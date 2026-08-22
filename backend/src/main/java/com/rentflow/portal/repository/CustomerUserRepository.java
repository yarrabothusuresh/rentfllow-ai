package com.rentflow.portal.repository;

import com.rentflow.portal.model.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerUserRepository extends JpaRepository<CustomerUser, UUID> {
    Optional<CustomerUser> findByEmailIgnoreCase(String email);
    Optional<CustomerUser> findByTenantIdAndEmailIgnoreCase(String tenantId, String email);
    Optional<CustomerUser> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
}
