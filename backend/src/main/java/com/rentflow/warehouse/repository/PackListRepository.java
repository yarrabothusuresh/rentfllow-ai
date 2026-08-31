package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.PackList;
import com.rentflow.warehouse.model.PackListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackListRepository extends JpaRepository<PackList, UUID> {
    List<PackList> findByTenantId(String tenantId);
    Optional<PackList> findByTenantIdAndId(String tenantId, UUID id);
    Optional<PackList> findByTenantIdAndPackListNumber(String tenantId, String packListNumber);
    Optional<PackList> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
    List<PackList> findByTenantIdAndStatus(String tenantId, PackListStatus status);
    List<PackList> findByTenantIdAndAssignedTo(String tenantId, String assignedTo);
    long countByTenantId(String tenantId);
}
