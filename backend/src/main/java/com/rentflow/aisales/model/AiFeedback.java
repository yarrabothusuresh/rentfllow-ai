package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_feedback", indexes = {
    @Index(name = "idx_aifeedback_tenant", columnList = "tenantId"),
    @Index(name = "idx_aifeedback_conv", columnList = "conversationId")
})
public class AiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID conversationId;

    private UUID messageId;

    @Column(nullable = false)
    private boolean helpful;

    private String reason; // INCORRECT_PRODUCT, INCORRECT_INTERPRETATION, POOR_RESPONSE, AVAILABILITY_ISSUE, PRICING_ISSUE, OTHER

    @Column(length = 2000)
    private String comments;

    private String submittedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

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

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }

    public boolean isHelpful() { return helpful; }
    public void setHelpful(boolean helpful) { this.helpful = helpful; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
