package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.PickList;
import com.rentflow.warehouse.model.PickListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PickListRepository extends JpaRepository<PickList, UUID> {
    List<PickList> findByTenantId(String tenantId);
    Optional<PickList> findByTenantIdAndId(String tenantId, UUID id);
    Optional<PickList> findByTenantIdAndPickListNumber(String tenantId, String pickListNumber);
    Optional<PickList> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
    List<PickList> findByTenantIdAndStatus(String tenantId, PickListStatus status);
    List<PickList> findByTenantIdAndAssignedTo(String tenantId, String assignedTo);
    long countByTenantId(String tenantId);
}
