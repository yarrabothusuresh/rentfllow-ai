package com.rentflow.automation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "automation_approvals", indexes = {
    @Index(name = "idx_aa_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_aa_rec_id", columnList = "recommendation_id"),
    @Index(name = "idx_aa_created_at", columnList = "created_at")
})
public class AutomationApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "recommendation_id", nullable = false)
    private UUID recommendationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AutomationActionType actionType;

    @Column(name = "action_payload_json", length = 4000)
    private String actionPayloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AutomationApprovalStatus status = AutomationApprovalStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy = "SYSTEM";

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approval_reason")
    private String approvalReason;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public AutomationApproval() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getRecommendationId() { return recommendationId; }
    public void setRecommendationId(UUID recommendationId) { this.recommendationId = recommendationId; }

    public AutomationActionType getActionType() { return actionType; }
    public void setActionType(AutomationActionType actionType) { this.actionType = actionType; }

    public String getActionPayloadJson() { return actionPayloadJson; }
    public void setActionPayloadJson(String actionPayloadJson) { this.actionPayloadJson = actionPayloadJson; }

    public AutomationApprovalStatus getStatus() { return status; }
    public void setStatus(AutomationApprovalStatus status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovalReason() { return approvalReason; }
    public void setApprovalReason(String approvalReason) { this.approvalReason = approvalReason; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
