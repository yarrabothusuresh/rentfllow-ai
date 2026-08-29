package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.InventoryTransfer;
import com.rentflow.inventory.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryTransferRepository extends JpaRepository<InventoryTransfer, UUID> {

    List<InventoryTransfer> findByTenantId(String tenantId);

    List<InventoryTransfer> findByTenantIdAndStatus(String tenantId, TransferStatus status);

    List<InventoryTransfer> findByTenantIdAndFromWarehouseIdOrToWarehouseId(String tenantId, UUID fromWhId, UUID toWhId);

    Optional<InventoryTransfer> findByTenantIdAndId(String tenantId, UUID id);

    Optional<InventoryTransfer> findByTenantIdAndTransferNumber(String tenantId, String transferNumber);
}
