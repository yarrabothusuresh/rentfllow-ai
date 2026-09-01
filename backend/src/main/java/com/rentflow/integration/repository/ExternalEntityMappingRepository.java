package com.rentflow.integration.repository;

import com.rentflow.integration.model.EntityType;
import com.rentflow.integration.model.ExternalEntityMapping;
import com.rentflow.integration.model.IntegrationProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalEntityMappingRepository extends JpaRepository<ExternalEntityMapping, UUID> {
    List<ExternalEntityMapping> findByTenantId(String tenantId);
    List<ExternalEntityMapping> findByTenantIdAndProvider(String tenantId, IntegrationProvider provider);
    Optional<ExternalEntityMapping> findByTenantIdAndProviderAndEntityTypeAndInternalId(
        String tenantId, IntegrationProvider provider, EntityType entityType, String internalId);
    Optional<ExternalEntityMapping> findByTenantIdAndProviderAndEntityTypeAndExternalId(
        String tenantId, IntegrationProvider provider, EntityType entityType, String externalId);
}
