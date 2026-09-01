package com.rentflow.integration.dto;

import com.rentflow.integration.model.EntityType;
import com.rentflow.integration.model.IntegrationProvider;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExternalEntityMappingDTO {
    private UUID id;
    private String tenantId;
    private IntegrationProvider provider;
    private EntityType entityType;
    private String internalId;
    private String externalId;
    private String syncStatus;
    private LocalDateTime lastSyncedAt;
    private String lastError;

    public ExternalEntityMappingDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public IntegrationProvider getProvider() { return provider; }
    public void setProvider(IntegrationProvider provider) { this.provider = provider; }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public String getInternalId() { return internalId; }
    public void setInternalId(String internalId) { this.internalId = internalId; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
