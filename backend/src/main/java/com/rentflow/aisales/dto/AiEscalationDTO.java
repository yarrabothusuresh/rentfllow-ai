package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.AiEscalationPriority;
import com.rentflow.aisales.model.AiEscalationReason;
import com.rentflow.aisales.model.AiEscalationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AiEscalationDTO {
    private UUID id;
    private UUID conversationId;
    private String conversationPublicId;
    private String customerName;
    private AiEscalationReason reason;
    private AiEscalationPriority priority;
    private AiEscalationStatus status;
    private String assignedTo;
    private String summary;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public AiEscalationDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getConversationPublicId() { return conversationPublicId; }
    public void setConversationPublicId(String conversationPublicId) { this.conversationPublicId = conversationPublicId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

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
