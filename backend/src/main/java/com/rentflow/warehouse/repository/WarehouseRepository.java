package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    List<Warehouse> findByTenantId(String tenantId);
    List<Warehouse> findByTenantIdAndActiveTrue(String tenantId);
    Optional<Warehouse> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Warehouse> findByTenantIdAndCode(String tenantId, String code);
    long countByTenantId(String tenantId);
}
