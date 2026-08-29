package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, UUID> {

    List<WarehouseStock> findByTenantId(String tenantId);

    List<WarehouseStock> findByTenantIdAndProductId(String tenantId, UUID productId);

    List<WarehouseStock> findByTenantIdAndWarehouseId(String tenantId, UUID warehouseId);

    Optional<WarehouseStock> findByTenantIdAndProductIdAndWarehouseId(String tenantId, UUID productId, UUID warehouseId);

    Optional<WarehouseStock> findByTenantIdAndId(String tenantId, UUID id);
}
