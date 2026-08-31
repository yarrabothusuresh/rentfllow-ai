package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.LoadList;
import com.rentflow.warehouse.model.LoadListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoadListRepository extends JpaRepository<LoadList, UUID> {
    List<LoadList> findByTenantId(String tenantId);
    Optional<LoadList> findByTenantIdAndId(String tenantId, UUID id);
    Optional<LoadList> findByTenantIdAndLoadListNumber(String tenantId, String loadListNumber);
    Optional<LoadList> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
    Optional<LoadList> findByTenantIdAndDeliveryId(String tenantId, UUID deliveryId);
    List<LoadList> findByTenantIdAndStatus(String tenantId, LoadListStatus status);
    List<LoadList> findByTenantIdAndAssignedTo(String tenantId, String assignedTo);
    long countByTenantId(String tenantId);
}
