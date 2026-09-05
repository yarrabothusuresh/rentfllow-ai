package com.rentflow.aisales.dto;

import java.time.LocalDateTime;

public class CopilotSourceReferenceDTO {

    private String type; // e.g., "BOOKING", "ANALYTICS", "CRM_LEAD", "WAREHOUSE_ORDER", "INVOICE", "DELIVERY"
    private String displayName;
    private String entityType;
    private String entityPublicId;
    private String route;
    private String metric;
    private String period;
    private Integer recordCount;
    private LocalDateTime generatedAt = LocalDateTime.now();

    public CopilotSourceReferenceDTO() {}

    public CopilotSourceReferenceDTO(String type, String displayName, String entityType, String entityPublicId, String route) {
        this.type = type;
        this.displayName = displayName;
        this.entityType = entityType;
        this.entityPublicId = entityPublicId;
        this.route = route;
    }

    public static CopilotSourceReferenceDTO of(String type, String displayName, String route) {
        return new CopilotSourceReferenceDTO(type, displayName, type, null, route);
    }

    public static CopilotSourceReferenceDTO entity(String entityType, String entityPublicId, String displayName, String route) {
        return new CopilotSourceReferenceDTO(entityType, displayName, entityType, entityPublicId, route);
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityPublicId() { return entityPublicId; }
    public void setEntityPublicId(String entityPublicId) { this.entityPublicId = entityPublicId; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
