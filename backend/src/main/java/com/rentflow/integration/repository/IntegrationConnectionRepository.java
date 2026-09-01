package com.rentflow.integration.repository;

import com.rentflow.integration.model.ConnectionStatus;
import com.rentflow.integration.model.IntegrationConnection;
import com.rentflow.integration.model.IntegrationProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection, UUID> {
    List<IntegrationConnection> findByTenantId(String tenantId);
    Optional<IntegrationConnection> findByTenantIdAndId(String tenantId, UUID id);
    Optional<IntegrationConnection> findByTenantIdAndProvider(String tenantId, IntegrationProvider provider);
    List<IntegrationConnection> findByTenantIdAndStatus(String tenantId, ConnectionStatus status);
}
