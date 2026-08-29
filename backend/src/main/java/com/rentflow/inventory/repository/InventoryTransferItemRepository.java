package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.InventoryTransferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransferItemRepository extends JpaRepository<InventoryTransferItem, UUID> {

    List<InventoryTransferItem> findByTransferId(UUID transferId);

    List<InventoryTransferItem> findByTransferIdAndProductId(UUID transferId, UUID productId);

    void deleteByTransferId(UUID transferId);
}
