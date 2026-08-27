package com.rentflow.claims.repository;

import com.rentflow.claims.model.ReplacementOrder;
import com.rentflow.claims.model.ReplacementOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReplacementOrderRepository extends JpaRepository<ReplacementOrder, UUID> {
    List<ReplacementOrder> findByTenantId(String tenantId);
    Optional<ReplacementOrder> findByTenantIdAndId(String tenantId, UUID id);
    Optional<ReplacementOrder> findByTenantIdAndReplacementNumber(String tenantId, String replacementNumber);
    List<ReplacementOrder> findByTenantIdAndClaimId(String tenantId, UUID claimId);
    List<ReplacementOrder> findByTenantIdAndProductId(String tenantId, UUID productId);
    List<ReplacementOrder> findByTenantIdAndStatus(String tenantId, ReplacementOrderStatus status);
    long countByTenantId(String tenantId);
    long countByTenantIdAndStatus(String tenantId, ReplacementOrderStatus status);
}
