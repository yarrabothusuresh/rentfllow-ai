package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.ChecklistStage;
import com.rentflow.warehouse.model.WarehouseOrderChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseOrderChecklistRepository extends JpaRepository<WarehouseOrderChecklist, UUID> {
    List<WarehouseOrderChecklist> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
    List<WarehouseOrderChecklist> findByTenantIdAndWarehouseOrderIdAndStage(String tenantId, UUID warehouseOrderId, ChecklistStage stage);
}
