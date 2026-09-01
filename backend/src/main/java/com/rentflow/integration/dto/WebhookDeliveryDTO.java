package com.rentflow.integration.dto;

import com.rentflow.integration.model.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class WebhookDeliveryDTO {
    private UUID id;
    private String tenantId;
    private UUID webhookEndpointId;
    private String webhookName;
    private String endpointUrl;
    private String integrationEventId;
    private String eventType;
    private int attemptNumber;
    private DeliveryStatus status;
    private Integer httpStatus;
    private String responseSummary;
    private String requestHeaders;
    private String requestPayload;
    private Long durationMs;
    private LocalDateTime nextRetryAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public WebhookDeliveryDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWebhookEndpointId() { return webhookEndpointId; }
    public void setWebhookEndpointId(UUID webhookEndpointId) { this.webhookEndpointId = webhookEndpointId; }

    public String getWebhookName() { return webhookName; }
    public void setWebhookName(String webhookName) { this.webhookName = webhookName; }

    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

    public String getIntegrationEventId() { return integrationEventId; }
    public void setIntegrationEventId(String integrationEventId) { this.integrationEventId = integrationEventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getResponseSummary() { return responseSummary; }
    public void setResponseSummary(String responseSummary) { this.responseSummary = responseSummary; }

    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
