package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.CycleCountStatus;
import com.rentflow.inventory.model.InventoryCycleCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryCycleCountRepository extends JpaRepository<InventoryCycleCount, UUID> {

    List<InventoryCycleCount> findByTenantId(String tenantId);

    List<InventoryCycleCount> findByTenantIdAndWarehouseId(String tenantId, UUID warehouseId);

    List<InventoryCycleCount> findByTenantIdAndStatus(String tenantId, CycleCountStatus status);

    Optional<InventoryCycleCount> findByTenantIdAndId(String tenantId, UUID id);

    Optional<InventoryCycleCount> findByTenantIdAndCountNumber(String tenantId, String countNumber);
}
