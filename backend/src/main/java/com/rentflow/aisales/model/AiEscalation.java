package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_escalations", indexes = {
    @Index(name = "idx_escalation_tenant", columnList = "tenantId"),
    @Index(name = "idx_escalation_conv", columnList = "conversationId"),
    @Index(name = "idx_escalation_status", columnList = "status")
})
public class AiEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiEscalationReason reason = AiEscalationReason.CUSTOMER_REQUEST;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiEscalationPriority priority = AiEscalationPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiEscalationStatus status = AiEscalationStatus.OPEN;

    private String assignedTo;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(length = 2000)
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public AiEscalationReason getReason() { return reason; }
    public void setReason(AiEscalationReason reason) { this.reason = reason; }

    public AiEscalationPriority getPriority() { return priority; }
    public void setPriority(AiEscalationPriority priority) { this.priority = priority; }

    public AiEscalationStatus getStatus() { return status; }
    public void setStatus(AiEscalationStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
