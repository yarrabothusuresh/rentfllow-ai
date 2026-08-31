package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.KitDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KitDefinitionRepository extends JpaRepository<KitDefinition, UUID> {
    List<KitDefinition> findByTenantId(String tenantId);
    Optional<KitDefinition> findByTenantIdAndId(String tenantId, UUID id);
    Optional<KitDefinition> findByTenantIdAndProductId(String tenantId, UUID productId);
}
