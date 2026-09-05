package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_copilot_action_proposals", indexes = {
    @Index(name = "idx_copilot_proposal_tenant", columnList = "tenant_id"),
    @Index(name = "idx_copilot_proposal_conv", columnList = "conversation_id"),
    @Index(name = "idx_copilot_proposal_status", columnList = "status")
})
public class CopilotActionProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private CopilotActionType actionType;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "safe_payload_json", length = 4000)
    private String safePayloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private CopilotRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CopilotActionStatus status;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public CopilotActionProposal() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public CopilotActionType getActionType() { return actionType; }
    public void setActionType(CopilotActionType actionType) { this.actionType = actionType; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getSafePayloadJson() { return safePayloadJson; }
    public void setSafePayloadJson(String safePayloadJson) { this.safePayloadJson = safePayloadJson; }

    public CopilotRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(CopilotRiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public CopilotActionStatus getStatus() { return status; }
    public void setStatus(CopilotActionStatus status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
