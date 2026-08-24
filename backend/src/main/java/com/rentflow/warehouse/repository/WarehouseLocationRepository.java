package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, UUID> {

    List<WarehouseLocation> findByTenantId(String tenantId);

    List<WarehouseLocation> findByTenantIdAndActiveTrue(String tenantId);

    Optional<WarehouseLocation> findByTenantIdAndCode(String tenantId, String code);
}
