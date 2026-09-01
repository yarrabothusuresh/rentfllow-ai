package com.rentflow.integration.dto;

import java.time.LocalDateTime;

public class IntegrationEnvelopeDTO<T> {
    private String eventId;
    private String eventType;
    private String eventVersion;
    private String tenantId;
    private LocalDateTime occurredAt;
    private T data;

    public IntegrationEnvelopeDTO() {}

    public IntegrationEnvelopeDTO(String eventId, String eventType, String eventVersion,
                                  String tenantId, LocalDateTime occurredAt, T data) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
        this.data = data;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventVersion() { return eventVersion; }
    public void setEventVersion(String eventVersion) { this.eventVersion = eventVersion; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
