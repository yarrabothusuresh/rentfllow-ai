package com.rentflow.calendar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operational_conflicts", indexes = {
    @Index(name = "idx_op_conf_tenant", columnList = "tenantId"),
    @Index(name = "idx_op_conf_status", columnList = "status"),
    @Index(name = "idx_op_conf_type", columnList = "conflictType")
})
public class OperationalConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConflictType conflictType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConflictSeverity severity;

    private String resourceName;
    private String resourceId;
    private String referenceId;
    private String referenceType;

    @Column(length = 2000)
    private String message;

    @Column(length = 2000)
    private String suggestedAction;

    @Column(nullable = false)
    private String status = "OPEN"; // OPEN, ACKNOWLEDGED, RESOLVED, OVERRIDDEN

    private String overriddenBy;
    @Column(length = 2000)
    private String overrideReason;
    private LocalDateTime overriddenAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OperationalConflict() {}

    public OperationalConflict(String tenantId, ConflictType conflictType, ConflictSeverity severity,
                               String resourceName, String resourceId, String referenceId, String referenceType,
                               String message, String suggestedAction) {
        this.tenantId = tenantId;
        this.conflictType = conflictType;
        this.severity = severity;
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.message = message;
        this.suggestedAction = suggestedAction;
        this.status = "OPEN";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public ConflictType getConflictType() { return conflictType; }
    public void setConflictType(ConflictType conflictType) { this.conflictType = conflictType; }

    public ConflictSeverity getSeverity() { return severity; }
    public void setSeverity(ConflictSeverity severity) { this.severity = severity; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOverriddenBy() { return overriddenBy; }
    public void setOverriddenBy(String overriddenBy) { this.overriddenBy = overriddenBy; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public LocalDateTime getOverriddenAt() { return overriddenAt; }
    public void setOverriddenAt(LocalDateTime overriddenAt) { this.overriddenAt = overriddenAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
