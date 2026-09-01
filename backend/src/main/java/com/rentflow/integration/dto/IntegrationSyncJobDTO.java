package com.rentflow.integration.dto;

import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.model.SyncJobStatus;
import com.rentflow.integration.model.SyncType;

import java.time.LocalDateTime;
import java.util.UUID;

public class IntegrationSyncJobDTO {
    private UUID id;
    private String tenantId;
    private UUID connectionId;
    private IntegrationProvider provider;
    private SyncType syncType;
    private SyncJobStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int recordsProcessed;
    private int recordsFailed;
    private String errorSummary;
    private LocalDateTime createdAt;

    public IntegrationSyncJobDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getConnectionId() { return connectionId; }
    public void setConnectionId(UUID connectionId) { this.connectionId = connectionId; }

    public IntegrationProvider getProvider() { return provider; }
    public void setProvider(IntegrationProvider provider) { this.provider = provider; }

    public SyncType getSyncType() { return syncType; }
    public void setSyncType(SyncType syncType) { this.syncType = syncType; }

    public SyncJobStatus getStatus() { return status; }
    public void setStatus(SyncJobStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(int recordsProcessed) { this.recordsProcessed = recordsProcessed; }

    public int getRecordsFailed() { return recordsFailed; }
    public void setRecordsFailed(int recordsFailed) { this.recordsFailed = recordsFailed; }

    public String getErrorSummary() { return errorSummary; }
    public void setErrorSummary(String errorSummary) { this.errorSummary = errorSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
