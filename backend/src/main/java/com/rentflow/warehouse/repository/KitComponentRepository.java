package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.KitComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KitComponentRepository extends JpaRepository<KitComponent, UUID> {
    List<KitComponent> findByKitDefinitionId(UUID kitDefinitionId);
    List<KitComponent> findByTenantIdAndKitDefinitionId(String tenantId, UUID kitDefinitionId);
}
