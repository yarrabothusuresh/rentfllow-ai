package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.PickVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PickVerificationRepository extends JpaRepository<PickVerification, UUID> {
    List<PickVerification> findByTenantId(String tenantId);
    Optional<PickVerification> findByTenantIdAndPickListId(String tenantId, UUID pickListId);
    Optional<PickVerification> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
}
