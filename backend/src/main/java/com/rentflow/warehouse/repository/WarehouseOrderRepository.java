package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseOrderRepository extends JpaRepository<WarehouseOrder, UUID> {

    List<WarehouseOrder> findByTenantId(String tenantId);

    Optional<WarehouseOrder> findByTenantIdAndId(String tenantId, UUID id);

    Optional<WarehouseOrder> findByTenantIdAndBookingId(String tenantId, UUID bookingId);

    List<WarehouseOrder> findByTenantIdAndBookingIdIn(String tenantId, List<UUID> bookingIds);

    List<WarehouseOrder> findByTenantIdAndStatus(String tenantId, WarehouseOrderStatus status);

    long countByTenantId(String tenantId);

    long countByTenantIdAndStatus(String tenantId, WarehouseOrderStatus status);
}
