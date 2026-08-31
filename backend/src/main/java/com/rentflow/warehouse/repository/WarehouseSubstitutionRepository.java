package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.SubstitutionStatus;
import com.rentflow.warehouse.model.WarehouseSubstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseSubstitutionRepository extends JpaRepository<WarehouseSubstitution, UUID> {
    List<WarehouseSubstitution> findByTenantId(String tenantId);
    Optional<WarehouseSubstitution> findByTenantIdAndId(String tenantId, UUID id);
    List<WarehouseSubstitution> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
    List<WarehouseSubstitution> findByTenantIdAndStatus(String tenantId, SubstitutionStatus status);
}
