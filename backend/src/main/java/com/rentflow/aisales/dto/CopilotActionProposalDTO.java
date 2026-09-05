package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.CopilotActionStatus;
import com.rentflow.aisales.model.CopilotActionType;
import com.rentflow.aisales.model.CopilotRiskLevel;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class CopilotActionProposalDTO {

    private UUID proposalId;
    private CopilotActionType actionType;
    private String targetType;
    private String targetId;
    private String summary;
    private CopilotRiskLevel riskLevel;
    private CopilotActionStatus status;
    private Map<String, Object> payload;
    private LocalDateTime expiresAt;
    private boolean requiresConfirmation;

    public CopilotActionProposalDTO() {}

    public UUID getProposalId() { return proposalId; }
    public void setProposalId(UUID proposalId) { this.proposalId = proposalId; }

    public CopilotActionType getActionType() { return actionType; }
    public void setActionType(CopilotActionType actionType) { this.actionType = actionType; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public CopilotRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(CopilotRiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public CopilotActionStatus getStatus() { return status; }
    public void setStatus(CopilotActionStatus status) { this.status = status; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRequiresConfirmation() { return requiresConfirmation; }
    public void setRequiresConfirmation(boolean requiresConfirmation) { this.requiresConfirmation = requiresConfirmation; }
}
