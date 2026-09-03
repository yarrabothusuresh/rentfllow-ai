package com.rentflow.crm.dto;

import com.rentflow.crm.model.ActivityDirection;
import com.rentflow.crm.model.ActivityType;
import com.rentflow.crm.model.CallOutcome;

import java.time.LocalDateTime;
import java.util.UUID;

public class LeadActivityDTO {
    private UUID id;
    private UUID leadId;
    private ActivityType type;
    private ActivityDirection direction;
    private String subject;
    private String summary;
    private String notes;
    private CallOutcome callOutcome;
    private String referenceType;
    private String referenceId;
    private LocalDateTime occurredAt;
    private String createdBy;
    private LocalDateTime createdAt;

    public LeadActivityDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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
