package com.rentflow.claims.repository;

import com.rentflow.claims.model.DamageClaimItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DamageClaimItemRepository extends JpaRepository<DamageClaimItem, UUID> {
    List<DamageClaimItem> findByTenantId(String tenantId);
    List<DamageClaimItem> findByTenantIdAndClaimId(String tenantId, UUID claimId);
    List<DamageClaimItem> findByTenantIdAndProductId(String tenantId, UUID productId);
    Optional<DamageClaimItem> findByTenantIdAndId(String tenantId, UUID id);
}
