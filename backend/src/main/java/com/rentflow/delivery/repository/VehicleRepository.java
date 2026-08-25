package com.rentflow.delivery.repository;

import com.rentflow.delivery.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByTenantIdAndId(String tenantId, UUID id);
    List<Vehicle> findByTenantId(String tenantId);
    List<Vehicle> findByTenantIdAndActiveTrue(String tenantId);
    long countByTenantId(String tenantId);
}
