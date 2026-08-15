package com.rentflow.ai.repository;

import com.rentflow.ai.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    List<InventoryTransaction> findByTenantIdAndProductIdOrderByCreatedAtDesc(String tenantId, UUID productId);
    List<InventoryTransaction> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
