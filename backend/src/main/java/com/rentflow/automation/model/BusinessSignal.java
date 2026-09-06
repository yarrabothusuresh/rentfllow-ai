package com.rentflow.automation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_signals", indexes = {
    @Index(name = "idx_bs_tenant_status_type", columnList = "tenant_id, status, signal_type"),
    @Index(name = "idx_bs_dedupe_key", columnList = "tenant_id, dedupe_key"),
    @Index(name = "idx_bs_source_entity", columnList = "tenant_id, source_entity_type, source_entity_id"),
    @Index(name = "idx_bs_detected_at", columnList = "detected_at")
})
public class BusinessSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false)
    private BusinessSignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private BusinessSignalCategory category;

    @Column(name = "source_entity_type", nullable = false)
    private String sourceEntityType;

    @Column(name = "source_entity_id", nullable = false)
    private String sourceEntityId;

    @Column(name = "source_entity_number")
    private String sourceEntityNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private BusinessSignalSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BusinessSignalStatus status = BusinessSignalStatus.ACTIVE;

    @Column(name = "dedupe_key", nullable = false)
    private String dedupeKey;

    @Column(name = "evidence_json", length = 4000)
    private String evidenceJson;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(name = "last_detected_at", nullable = false)
    private LocalDateTime lastDetectedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BusinessSignal() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public BusinessSignalType getSignalType() { return signalType; }
    public void setSignalType(BusinessSignalType signalType) { this.signalType = signalType; }

    public BusinessSignalCategory getCategory() { return category; }
    public void setCategory(BusinessSignalCategory category) { this.category = category; }

    public String getSourceEntityType() { return sourceEntityType; }
    public void setSourceEntityType(String sourceEntityType) { this.sourceEntityType = sourceEntityType; }

    public String getSourceEntityId() { return sourceEntityId; }
    public void setSourceEntityId(String sourceEntityId) { this.sourceEntityId = sourceEntityId; }

    public String getSourceEntityNumber() { return sourceEntityNumber; }
    public void setSourceEntityNumber(String sourceEntityNumber) { this.sourceEntityNumber = sourceEntityNumber; }

    public BusinessSignalSeverity getSeverity() { return severity; }
    public void setSeverity(BusinessSignalSeverity severity) { this.severity = severity; }

    public BusinessSignalStatus getStatus() { return status; }
    public void setStatus(BusinessSignalStatus status) { this.status = status; }

    public String getDedupeKey() { return dedupeKey; }
    public void setDedupeKey(String dedupeKey) { this.dedupeKey = dedupeKey; }

    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public LocalDateTime getLastDetectedAt() { return lastDetectedAt; }
    public void setLastDetectedAt(LocalDateTime lastDetectedAt) { this.lastDetectedAt = lastDetectedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
