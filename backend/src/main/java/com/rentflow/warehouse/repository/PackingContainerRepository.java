package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.ContainerStatus;
import com.rentflow.warehouse.model.PackingContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackingContainerRepository extends JpaRepository<PackingContainer, UUID> {
    List<PackingContainer> findByTenantId(String tenantId);
    Optional<PackingContainer> findByTenantIdAndId(String tenantId, UUID id);
    Optional<PackingContainer> findByTenantIdAndContainerCode(String tenantId, String containerCode);
    List<PackingContainer> findByTenantIdAndStatus(String tenantId, ContainerStatus status);
}
