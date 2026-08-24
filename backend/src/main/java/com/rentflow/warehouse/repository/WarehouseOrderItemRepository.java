package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.WarehouseOrderItem;
import com.rentflow.warehouse.model.WarehouseOrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseOrderItemRepository extends JpaRepository<WarehouseOrderItem, UUID> {

    List<WarehouseOrderItem> findByWarehouseOrderId(UUID warehouseOrderId);

    Optional<WarehouseOrderItem> findByWarehouseOrderIdAndId(UUID warehouseOrderId, UUID itemId);

    List<WarehouseOrderItem> findByWarehouseOrderIdAndStatus(UUID warehouseOrderId, WarehouseOrderItemStatus status);
}
