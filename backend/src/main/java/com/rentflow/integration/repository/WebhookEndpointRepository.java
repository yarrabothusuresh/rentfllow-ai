package com.rentflow.integration.repository;

import com.rentflow.integration.model.WebhookEndpoint;
import com.rentflow.integration.model.WebhookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {
    List<WebhookEndpoint> findByTenantId(String tenantId);
    Optional<WebhookEndpoint> findByTenantIdAndId(String tenantId, UUID id);
    List<WebhookEndpoint> findByTenantIdAndStatus(String tenantId, WebhookStatus status);
    long countByTenantIdAndStatus(String tenantId, WebhookStatus status);
}
