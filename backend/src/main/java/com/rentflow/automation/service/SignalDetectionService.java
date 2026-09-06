package com.rentflow.automation.service;

import com.rentflow.automation.detector.BusinessSignalDetector;
import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AutomationExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SignalDetectionService {

    private static final Logger log = LoggerFactory.getLogger(SignalDetectionService.class);

    @Autowired
    private List<BusinessSignalDetector> detectors;

    @Autowired
    private BusinessSignalService signalService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private AutomationRuleService ruleService;

    @Autowired
    private AutomationApprovalService approvalService;

    @Autowired
    private AutomationExecutionService executionService;

    @Autowired
    private AutomationExecutionRepository executionRepository;

    @Transactional
    public List<AiRecommendation> runDetection(String tenantId) {
        log.info("Running business signal detection for tenant: {}", tenantId);
        List<AiRecommendation> createdRecommendations = new ArrayList<>();

        ruleService.ensureDefaultRules(tenantId);

        for (BusinessSignalDetector detector : detectors) {
            try {
                List<DetectedSignal> detectedSignals = detector.detect(tenantId);
                for (DetectedSignal detected : detectedSignals) {
                    BusinessSignal signal = signalService.recordOrUpdateSignal(tenantId, detected);

                    // Find matching rule
                    Optional<AutomationRule> ruleOpt = ruleService.findMatchingRule(tenantId, detected.signalType());
                    UUID ruleId = ruleOpt.map(AutomationRule::getId).orElse(null);
                    AutomationMode mode = ruleOpt.map(AutomationRule::getMode).orElse(AutomationMode.APPROVAL_REQUIRED);

                    AiRecommendation recommendation = recommendationService.createOrUpdateRecommendation(
                        tenantId, signal, detected, ruleId
                    );
                    createdRecommendations.add(recommendation);

                    // Evaluate automated action
                    if (ruleOpt.isPresent() && ruleOpt.get().isEnabled() && detected.suggestedActionType() != null) {
                        AutomationRule rule = ruleOpt.get();

                        // Loop Safeguard: check max executions today for this rule
                        long todayExecs = executionRepository.countByTenantIdAndRuleIdAndCreatedAtAfter(
                            tenantId, rule.getId(), LocalDateTime.now().withHour(0).withMinute(0)
                        );
                        if (todayExecs >= rule.getMaxExecutionsPerDay()) {
                            log.warn("Rule {} exceeded daily maximum execution limit ({}/{}). Skipping automation.", 
                                rule.getRuleCode(), todayExecs, rule.getMaxExecutionsPerDay());
                            continue;
                        }

                        if (mode == AutomationMode.AUTO_EXECUTE_LOW_RISK && isLowRiskAction(detected.suggestedActionType())) {
                            String idempotencyKey = tenantId + ":AUTO:" + rule.getRuleCode() + ":" + detected.sourceEntityType() + ":" + detected.sourceEntityId();
                            executionService.executeAction(
                                tenantId,
                                recommendation.getId(),
                                null,
                                rule.getId(),
                                detected.suggestedActionType(),
                                recommendation.getSuggestedActionPayloadJson(),
                                idempotencyKey,
                                "AUTO_SYSTEM"
                            );
                        } else if (mode == AutomationMode.APPROVAL_REQUIRED) {
                            approvalService.createApprovalRequest(
                                tenantId,
                                recommendation.getId(),
                                detected.suggestedActionType(),
                                recommendation.getSuggestedActionPayloadJson(),
                                "SYSTEM"
                            );
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Detector {} failed for tenant {}: {}", detector.getClass().getSimpleName(), tenantId, e.getMessage(), e);
            }
        }

        return createdRecommendations;
    }

    private boolean isLowRiskAction(AutomationActionType actionType) {
        return actionType == AutomationActionType.CREATE_INTERNAL_TASK ||
               actionType == AutomationActionType.RETRY_INTEGRATION_EVENT ||
               actionType == AutomationActionType.RETRY_WEBHOOK_DELIVERY;
    }
}
