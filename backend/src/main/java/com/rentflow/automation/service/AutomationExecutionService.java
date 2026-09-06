package com.rentflow.automation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.automation.action.*;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AutomationAuditRepository;
import com.rentflow.automation.repository.AutomationExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AutomationExecutionService {

    private static final Logger log = LoggerFactory.getLogger(AutomationExecutionService.class);

    @Autowired
    private AutomationExecutionRepository executionRepository;

    @Autowired
    private AutomationAuditRepository auditRepository;

    @Autowired
    private AutomationActionRegistry actionRegistry;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public AutomationExecution executeAction(
        String tenantId,
        UUID recommendationId,
        UUID approvalId,
        UUID ruleId,
        AutomationActionType actionType,
        String actionPayloadJson,
        String idempotencyKey,
        String executedBy
    ) {
        // 1. Idempotency Check
        Optional<AutomationExecution> existingExec = executionRepository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);
        if (existingExec.isPresent()) {
            log.info("Idempotency match found for key: {}. Returning existing execution.", idempotencyKey);
            return existingExec.get();
        }

        AutomationExecution execution = new AutomationExecution();
        execution.setTenantId(tenantId);
        execution.setRecommendationId(recommendationId);
        execution.setApprovalId(approvalId);
        execution.setRuleId(ruleId);
        execution.setActionType(actionType);
        execution.setIdempotencyKey(idempotencyKey);
        execution.setActionPayloadJson(actionPayloadJson);
        execution.setExecutedBy(executedBy);
        execution.setExecutionStatus(AutomationExecutionStatus.EXECUTING);
        execution.setCreatedAt(LocalDateTime.now());
        execution.setStartedAt(LocalDateTime.now());
        execution.setAttemptCount(1);
        execution = executionRepository.save(execution);

        // 2. Resolve Handler
        AutomationActionHandler handler = actionRegistry.getHandler(actionType).orElse(null);
        if (handler == null) {
            execution.setExecutionStatus(AutomationExecutionStatus.FAILED);
            execution.setErrorDetails("No registered action handler for " + actionType);
            execution.setCompletedAt(LocalDateTime.now());
            recordAudit(tenantId, "EXECUTION_FAILED", "ACTION", actionType.name(), executedBy, "No handler for " + actionType);
            return executionRepository.save(execution);
        }

        // 3. Parse Payload
        Map<String, Object> payload = Collections.emptyMap();
        try {
            if (actionPayloadJson != null && !actionPayloadJson.isBlank()) {
                payload = objectMapper.readValue(actionPayloadJson, new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse action payload JSON: {}", e.getMessage());
        }

        // 4. Pre-Execution Revalidation
        RevalidationResult reval = handler.revalidate(tenantId, payload);
        if (!reval.isValid()) {
            if (reval.alreadyCompleted()) {
                execution.setExecutionStatus(AutomationExecutionStatus.SKIPPED);
                execution.setResultSummary("Already completed: " + reval.reason());
                execution.setCompletedAt(LocalDateTime.now());
                if (recommendationId != null) {
                    recommendationService.resolveRecommendation(recommendationId);
                }
                recordAudit(tenantId, "EXECUTION_SKIPPED", "ACTION", actionType.name(), executedBy, reval.reason());
                return executionRepository.save(execution);
            } else {
                execution.setExecutionStatus(AutomationExecutionStatus.STALE);
                execution.setResultSummary("Pre-execution check failed: " + reval.reason());
                execution.setCompletedAt(LocalDateTime.now());
                recordAudit(tenantId, "EXECUTION_STALE", "ACTION", actionType.name(), executedBy, reval.reason());
                return executionRepository.save(execution);
            }
        }

        // 5. Execute
        try {
            ActionResult result = handler.execute(tenantId, payload, executedBy);
            execution.setCompletedAt(LocalDateTime.now());
            if (result.success()) {
                execution.setExecutionStatus(AutomationExecutionStatus.EXECUTED);
                execution.setResultSummary(result.summary());
                if (recommendationId != null) {
                    recommendationService.resolveRecommendation(recommendationId);
                }
                recordAudit(tenantId, "ACTION_EXECUTED", "ACTION", actionType.name(), executedBy, result.summary());
            } else {
                execution.setExecutionStatus(AutomationExecutionStatus.FAILED);
                execution.setResultSummary(result.summary());
                execution.setErrorDetails(result.errorDetails());
                recordAudit(tenantId, "EXECUTION_FAILED", "ACTION", actionType.name(), executedBy, result.errorDetails());
            }
        } catch (Exception ex) {
            log.error("Execution error executing {}: {}", actionType, ex.getMessage(), ex);
            execution.setExecutionStatus(AutomationExecutionStatus.FAILED);
            execution.setResultSummary("Unexpected execution exception");
            execution.setErrorDetails(ex.getMessage());
            execution.setCompletedAt(LocalDateTime.now());
            recordAudit(tenantId, "EXECUTION_FAILED", "ACTION", actionType.name(), executedBy, ex.getMessage());
        }

        return executionRepository.save(execution);
    }

    private void recordAudit(String tenantId, String action, String entityType, String entityId, String performedBy, String details) {
        try {
            AutomationAudit audit = new AutomationAudit(tenantId, action, entityType, entityId, performedBy, details);
            auditRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to record automation audit: {}", e.getMessage());
        }
    }
}
