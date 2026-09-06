package com.rentflow.automation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "automation_executions", indexes = {
    @Index(name = "idx_ae_tenant_status", columnList = "tenant_id, execution_status"),
    @Index(name = "idx_ae_idempotency", columnList = "tenant_id, idempotency_key", unique = true),
    @Index(name = "idx_ae_rec_id", columnList = "recommendation_id"),
    @Index(name = "idx_ae_created_at", columnList = "created_at")
})
public class AutomationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "recommendation_id")
    private UUID recommendationId;

    @Column(name = "approval_id")
    private UUID approvalId;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AutomationActionType actionType;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false)
    private AutomationExecutionStatus executionStatus = AutomationExecutionStatus.PROPOSED;

    @Column(name = "action_payload_json", length = 4000)
    private String actionPayloadJson;

    @Column(name = "result_summary", length = 2000)
    private String resultSummary;

    @Column(name = "error_details", length = 4000)
    private String errorDetails;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "executed_by", nullable = false)
    private String executedBy = "AUTO_SYSTEM";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public AutomationExecution() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getRecommendationId() { return recommendationId; }
    public void setRecommendationId(UUID recommendationId) { this.recommendationId = recommendationId; }

    public UUID getApprovalId() { return approvalId; }
    public void setApprovalId(UUID approvalId) { this.approvalId = approvalId; }

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }

    public AutomationActionType getActionType() { return actionType; }
    public void setActionType(AutomationActionType actionType) { this.actionType = actionType; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public AutomationExecutionStatus getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(AutomationExecutionStatus executionStatus) { this.executionStatus = executionStatus; }

    public String getActionPayloadJson() { return actionPayloadJson; }
    public void setActionPayloadJson(String actionPayloadJson) { this.actionPayloadJson = actionPayloadJson; }

    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
