package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.InventoryCycleCountItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryCycleCountItemRepository extends JpaRepository<InventoryCycleCountItem, UUID> {

    List<InventoryCycleCountItem> findByCycleCountId(UUID cycleCountId);

    void deleteByCycleCountId(UUID cycleCountId);
}
