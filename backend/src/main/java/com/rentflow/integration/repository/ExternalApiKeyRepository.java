package com.rentflow.integration.repository;

import com.rentflow.integration.model.ApiKeyStatus;
import com.rentflow.integration.model.ExternalApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalApiKeyRepository extends JpaRepository<ExternalApiKey, UUID> {
    List<ExternalApiKey> findByTenantId(String tenantId);
    Optional<ExternalApiKey> findByTenantIdAndId(String tenantId, UUID id);
    Optional<ExternalApiKey> findByKeyHash(String keyHash);
    List<ExternalApiKey> findByTenantIdAndStatus(String tenantId, ApiKeyStatus status);
}
