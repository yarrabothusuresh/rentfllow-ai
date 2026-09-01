package com.rentflow.integration.dto;

import com.rentflow.integration.model.WebhookStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class WebhookEndpointDTO {
    private UUID id;
    private String tenantId;
    private String name;
    private String endpointUrl;
    private String maskedSecret;
    private WebhookStatus status;
    private List<String> subscribedEvents;
    private String description;
    private long totalDeliveries;
    private long successfulDeliveries;
    private double successRate;
    private LocalDateTime lastDeliveryAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WebhookEndpointDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

    public String getMaskedSecret() { return maskedSecret; }
    public void setMaskedSecret(String maskedSecret) { this.maskedSecret = maskedSecret; }

    public WebhookStatus getStatus() { return status; }
    public void setStatus(WebhookStatus status) { this.status = status; }

    public List<String> getSubscribedEvents() { return subscribedEvents; }
    public void setSubscribedEvents(List<String> subscribedEvents) { this.subscribedEvents = subscribedEvents; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(long totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public long getSuccessfulDeliveries() { return successfulDeliveries; }
    public void setSuccessfulDeliveries(long successfulDeliveries) { this.successfulDeliveries = successfulDeliveries; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public LocalDateTime getLastDeliveryAt() { return lastDeliveryAt; }
    public void setLastDeliveryAt(LocalDateTime lastDeliveryAt) { this.lastDeliveryAt = lastDeliveryAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
