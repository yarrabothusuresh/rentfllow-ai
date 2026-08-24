package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.WarehouseAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WarehouseAuditRepository extends JpaRepository<WarehouseAudit, UUID> {

    List<WarehouseAudit> findByTenantIdAndWarehouseOrderIdOrderByTimestampDesc(String tenantId, UUID warehouseOrderId);

    List<WarehouseAudit> findByTenantIdAndBookingIdOrderByTimestampDesc(String tenantId, UUID bookingId);
}
