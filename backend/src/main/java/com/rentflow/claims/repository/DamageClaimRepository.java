package com.rentflow.claims.repository;

import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.DamageClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DamageClaimRepository extends JpaRepository<DamageClaim, UUID> {
    List<DamageClaim> findByTenantId(String tenantId);
    Optional<DamageClaim> findByTenantIdAndId(String tenantId, UUID id);
    Optional<DamageClaim> findByTenantIdAndClaimNumber(String tenantId, String claimNumber);
    Optional<DamageClaim> findByTenantIdAndReturnOrderId(String tenantId, UUID returnOrderId);
    List<DamageClaim> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    List<DamageClaim> findByTenantIdAndBookingId(String tenantId, UUID bookingId);
    List<DamageClaim> findByTenantIdAndStatus(String tenantId, ClaimStatus status);
    long countByTenantId(String tenantId);
    long countByTenantIdAndStatus(String tenantId, ClaimStatus status);
}
