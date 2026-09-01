package com.rentflow.integration.dto;

import com.rentflow.integration.model.ConnectionStatus;
import com.rentflow.integration.model.ConnectorCapability;
import com.rentflow.integration.model.IntegrationProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class IntegrationConnectionDTO {
    private UUID id;
    private String tenantId;
    private IntegrationProvider provider;
    private String name;
    private ConnectionStatus status;
    private String configuration;
    private String maskedCredential;
    private List<ConnectorCapability> capabilities;
    private LocalDateTime lastConnectedAt;
    private LocalDateTime lastSyncAt;
    private String lastError;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IntegrationConnectionDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public IntegrationProvider getProvider() { return provider; }
    public void setProvider(IntegrationProvider provider) { this.provider = provider; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus status) { this.status = status; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }

    public String getMaskedCredential() { return maskedCredential; }
    public void setMaskedCredential(String maskedCredential) { this.maskedCredential = maskedCredential; }

    public List<ConnectorCapability> getCapabilities() { return capabilities; }
    public void setCapabilities(List<ConnectorCapability> capabilities) { this.capabilities = capabilities; }

    public LocalDateTime getLastConnectedAt() { return lastConnectedAt; }
    public void setLastConnectedAt(LocalDateTime lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
