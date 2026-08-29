package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.MovementType;
import com.rentflow.inventory.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByTenantId(String tenantId);

    Page<StockMovement> findByTenantId(String tenantId, Pageable pageable);

    List<StockMovement> findByTenantIdAndProductId(String tenantId, UUID productId);

    Page<StockMovement> findByTenantIdAndProductId(String tenantId, UUID productId, Pageable pageable);

    List<StockMovement> findByTenantIdAndInventoryItemId(String tenantId, UUID inventoryItemId);

    Page<StockMovement> findByTenantIdAndInventoryItemId(String tenantId, UUID inventoryItemId, Pageable pageable);

    List<StockMovement> findByTenantIdAndWarehouseId(String tenantId, UUID warehouseId);

    List<StockMovement> findByTenantIdAndMovementType(String tenantId, MovementType movementType);

    List<StockMovement> findByTenantIdAndReferenceId(String tenantId, UUID referenceId);

    List<StockMovement> findByTenantIdAndCreatedAtBetween(String tenantId, LocalDateTime start, LocalDateTime end);
}
