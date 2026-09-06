package com.rentflow.automation.service;

import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AiRecommendationRepository;
import com.rentflow.automation.repository.AutomationApprovalRepository;
import com.rentflow.automation.repository.AutomationAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AutomationApprovalService {

    @Autowired
    private AutomationApprovalRepository approvalRepository;

    @Autowired
    private AiRecommendationRepository recommendationRepository;

    @Autowired
    private AutomationExecutionService executionService;

    @Autowired
    private AutomationAuditRepository auditRepository;

    public Page<AutomationApproval> getPendingApprovals(String tenantId, Pageable pageable) {
        return approvalRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, AutomationApprovalStatus.PENDING, pageable);
    }

    public Page<AutomationApproval> getAllApprovals(String tenantId, Pageable pageable) {
        return approvalRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }

    public Optional<AutomationApproval> getApprovalById(UUID id, String tenantId) {
        return approvalRepository.findByIdAndTenantId(id, tenantId);
    }

    @Transactional
    public AutomationApproval createApprovalRequest(String tenantId, UUID recommendationId, AutomationActionType actionType, String payloadJson, String requestedBy) {
        // Check if there is already a pending approval for this recommendation
        Optional<AutomationApproval> existing = approvalRepository.findByTenantIdAndRecommendationIdAndStatus(tenantId, recommendationId, AutomationApprovalStatus.PENDING);
        if (existing.isPresent()) {
            return existing.get();
        }

        AutomationApproval approval = new AutomationApproval();
        approval.setTenantId(tenantId);
        approval.setRecommendationId(recommendationId);
        approval.setActionType(actionType);
        approval.setActionPayloadJson(payloadJson);
        approval.setStatus(AutomationApprovalStatus.PENDING);
        approval.setRequestedBy(requestedBy != null ? requestedBy : "SYSTEM");
        approval.setCreatedAt(LocalDateTime.now());
        approval.setExpiresAt(LocalDateTime.now().plusDays(3));

        return approvalRepository.save(approval);
    }

    @Transactional
    public AutomationExecution approveAndExecute(UUID approvalId, String tenantId, String approvedBy, String reason, String userRole) {
        if ("ROLE_CUSTOMER".equalsIgnoreCase(userRole) || "CUSTOMER".equalsIgnoreCase(userRole)) {
            throw new SecurityException("Customers are not permitted to approve internal business automations");
        }

        AutomationApproval approval = approvalRepository.findByIdAndTenantId(approvalId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (approval.getStatus() != AutomationApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval is already in status " + approval.getStatus());
        }

        approval.setStatus(AutomationApprovalStatus.APPROVED);
        approval.setApprovedBy(approvedBy);
        approval.setApprovalReason(reason);
        approval.setDecidedAt(LocalDateTime.now());
        approvalRepository.save(approval);

        // Update recommendation status
        AiRecommendation rec = recommendationRepository.findByIdAndTenantId(approval.getRecommendationId(), tenantId).orElse(null);
        UUID ruleId = null;
        if (rec != null) {
            rec.setStatus(RecommendationStatus.APPROVED);
            rec.setReviewedBy(approvedBy);
            rec.setReviewedAt(LocalDateTime.now());
            ruleId = rec.getRuleId();
            recommendationRepository.save(rec);
        }

        // Record Audit
        auditRepository.save(new AutomationAudit(
            tenantId,
            "ACTION_APPROVED",
            "APPROVAL",
            approval.getId().toString(),
            approvedBy,
            "Approved action " + approval.getActionType() + ": " + (reason != null ? reason : "Standard approval")
        ));

        // Execute via execution service
        String idempotencyKey = tenantId + ":APPROVAL:" + approval.getId();
        return executionService.executeAction(
            tenantId,
            approval.getRecommendationId(),
            approval.getId(),
            ruleId,
            approval.getActionType(),
            approval.getActionPayloadJson(),
            idempotencyKey,
            approvedBy
        );
    }

    @Transactional
    public AutomationApproval rejectApproval(UUID approvalId, String tenantId, String rejectedBy, String reason, String userRole) {
        if ("ROLE_CUSTOMER".equalsIgnoreCase(userRole) || "CUSTOMER".equalsIgnoreCase(userRole)) {
            throw new SecurityException("Customers are not permitted to reject internal business automations");
        }

        AutomationApproval approval = approvalRepository.findByIdAndTenantId(approvalId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (approval.getStatus() != AutomationApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval is already in status " + approval.getStatus());
        }

        approval.setStatus(AutomationApprovalStatus.REJECTED);
        approval.setApprovedBy(rejectedBy);
        approval.setRejectionReason(reason);
        approval.setDecidedAt(LocalDateTime.now());
        approval = approvalRepository.save(approval);

        // Update recommendation status
        AiRecommendation rec = recommendationRepository.findByIdAndTenantId(approval.getRecommendationId(), tenantId).orElse(null);
        if (rec != null) {
            rec.setStatus(RecommendationStatus.REJECTED);
            rec.setDismissReason(reason);
            rec.setReviewedBy(rejectedBy);
            rec.setReviewedAt(LocalDateTime.now());
            recommendationRepository.save(rec);
        }

        auditRepository.save(new AutomationAudit(
            tenantId,
            "ACTION_REJECTED",
            "APPROVAL",
            approval.getId().toString(),
            rejectedBy,
            "Rejected action " + approval.getActionType() + ": " + reason
        ));

        return approval;
    }
}
