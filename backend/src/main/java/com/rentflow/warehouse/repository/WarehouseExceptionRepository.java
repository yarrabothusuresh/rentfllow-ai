package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.WarehouseException;
import com.rentflow.warehouse.model.WarehouseExceptionSeverity;
import com.rentflow.warehouse.model.WarehouseExceptionStatus;
import com.rentflow.warehouse.model.WarehouseExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseExceptionRepository extends JpaRepository<WarehouseException, UUID> {
    List<WarehouseException> findByTenantId(String tenantId);
    Optional<WarehouseException> findByTenantIdAndId(String tenantId, UUID id);
    List<WarehouseException> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);
    List<WarehouseException> findByTenantIdAndPickListId(String tenantId, UUID pickListId);
    List<WarehouseException> findByTenantIdAndStatus(String tenantId, WarehouseExceptionStatus status);
    List<WarehouseException> findByTenantIdAndSeverity(String tenantId, WarehouseExceptionSeverity severity);
    List<WarehouseException> findByTenantIdAndType(String tenantId, WarehouseExceptionType type);
    long countByTenantIdAndStatus(String tenantId, WarehouseExceptionStatus status);
    long countByTenantIdAndWarehouseOrderIdAndStatus(String tenantId, UUID warehouseOrderId, WarehouseExceptionStatus status);
    long countByTenantIdAndWarehouseOrderIdAndSeverityAndStatus(String tenantId, UUID warehouseOrderId, WarehouseExceptionSeverity severity, WarehouseExceptionStatus status);
}
