package com.rentflow.claims.repository;

import com.rentflow.claims.model.ClaimAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimAuditRepository extends JpaRepository<ClaimAudit, UUID> {
    List<ClaimAudit> findByTenantId(String tenantId);
    List<ClaimAudit> findByTenantIdAndClaimIdOrderByTimestampDesc(String tenantId, UUID claimId);
}
