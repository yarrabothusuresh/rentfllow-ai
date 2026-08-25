package com.rentflow.delivery.repository;

import com.rentflow.delivery.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Driver> findByTenantIdAndUserId(String tenantId, UUID userId);
    List<Driver> findByTenantId(String tenantId);
    List<Driver> findByTenantIdAndActiveTrue(String tenantId);
    long countByTenantId(String tenantId);
}
