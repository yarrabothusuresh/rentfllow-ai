package com.rentflow.integration.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_entity_mapping", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "provider", "entity_type", "internal_id"})
})
public class ExternalEntityMapping {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private IntegrationProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "internal_id", nullable = false)
    private String internalId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "sync_status", nullable = false)
    private String syncStatus; // SYNCED, PENDING, ERROR

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExternalEntityMapping() {}

    public ExternalEntityMapping(String tenantId, IntegrationProvider provider, EntityType entityType,
                                 String internalId, String externalId, String syncStatus) {
        this.tenantId = tenantId;
        this.provider = provider;
        this.entityType = entityType;
        this.internalId = internalId;
        this.externalId = externalId;
        this.syncStatus = syncStatus;
        this.lastSyncedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastSyncedAt == null) {
            lastSyncedAt = LocalDateTime.now();
        }
        if (syncStatus == null) {
            syncStatus = "SYNCED";
        }
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
