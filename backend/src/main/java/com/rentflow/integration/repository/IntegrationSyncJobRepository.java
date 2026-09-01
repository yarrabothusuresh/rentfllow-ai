package com.rentflow.integration.repository;

import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.model.IntegrationSyncJob;
import com.rentflow.integration.model.SyncJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationSyncJobRepository extends JpaRepository<IntegrationSyncJob, UUID> {
    List<IntegrationSyncJob> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<IntegrationSyncJob> findByTenantIdAndConnectionIdOrderByCreatedAtDesc(String tenantId, UUID connectionId);
    List<IntegrationSyncJob> findByTenantIdAndStatus(String tenantId, SyncJobStatus status);
}
