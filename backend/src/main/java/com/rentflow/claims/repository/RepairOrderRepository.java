package com.rentflow.claims.repository;

import com.rentflow.claims.model.RepairOrder;
import com.rentflow.claims.model.RepairOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, UUID> {
    List<RepairOrder> findByTenantId(String tenantId);
    Optional<RepairOrder> findByTenantIdAndId(String tenantId, UUID id);
    Optional<RepairOrder> findByTenantIdAndRepairNumber(String tenantId, String repairNumber);
    List<RepairOrder> findByTenantIdAndClaimId(String tenantId, UUID claimId);
    List<RepairOrder> findByTenantIdAndProductId(String tenantId, UUID productId);
    List<RepairOrder> findByTenantIdAndStatus(String tenantId, RepairOrderStatus status);
    long countByTenantId(String tenantId);
    long countByTenantIdAndStatus(String tenantId, RepairOrderStatus status);
}
