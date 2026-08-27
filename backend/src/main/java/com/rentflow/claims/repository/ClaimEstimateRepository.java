package com.rentflow.claims.repository;

import com.rentflow.claims.model.ClaimEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimEstimateRepository extends JpaRepository<ClaimEstimate, UUID> {
    List<ClaimEstimate> findByTenantId(String tenantId);
    List<ClaimEstimate> findByTenantIdAndClaimIdOrderByVersionDesc(String tenantId, UUID claimId);
    Optional<ClaimEstimate> findFirstByTenantIdAndClaimIdOrderByVersionDesc(String tenantId, UUID claimId);
}
