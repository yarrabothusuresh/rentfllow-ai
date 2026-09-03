package com.rentflow.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lead_activities", indexes = {
    @Index(name = "idx_lead_act_tenant_lead", columnList = "tenantId, leadId"),
    @Index(name = "idx_lead_act_occurred", columnList = "occurredAt")
})
public class LeadActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    private ActivityDirection direction = ActivityDirection.INTERNAL;

    private String subject;

    @Column(length = 2000)
    private String summary;

    @Column(length = 4000)
    private String notes;

    @Enumerated(EnumType.STRING)
    private CallOutcome callOutcome;

    private String referenceType;
    private String referenceId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    private String createdBy;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.occurredAt == null) this.occurredAt = LocalDateTime.now();
        if (this.direction == null) this.direction = ActivityDirection.INTERNAL;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }

    public ActivityDirection getDirection() { return direction; }
    public void setDirection(ActivityDirection direction) { this.direction = direction; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public CallOutcome getCallOutcome() { return callOutcome; }
    public void setCallOutcome(CallOutcome callOutcome) { this.callOutcome = callOutcome; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
