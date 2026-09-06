package com.rentflow.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AiRecommendationRepository;
import com.rentflow.automation.repository.BusinessSignalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private AiRecommendationRepository recommendationRepository;

    @Autowired
    private BusinessSignalRepository businessSignalRepository;

    @Autowired
    private RecommendationExplanationService explanationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public AiRecommendation createOrUpdateRecommendation(String tenantId, BusinessSignal signal, DetectedSignal detected, UUID ruleId) {
        // Check if there is already an active recommendation for this signal
        List<AiRecommendation> existingList = recommendationRepository.findByTenantIdAndSignalId(tenantId, signal.getId());
        for (AiRecommendation r : existingList) {
            if (r.getStatus() == RecommendationStatus.NEW || r.getStatus() == RecommendationStatus.REVIEWED || r.getStatus() == RecommendationStatus.APPROVED) {
                // Update priority / evidence
                r.setUpdatedAt(LocalDateTime.now());
                return recommendationRepository.save(r);
            }
        }

        long count = recommendationRepository.countByTenantId(tenantId);
        String recNumber = String.format("REC-%06d", count + 1);

        RecommendationExplanationService.ExplanationResult explanation = explanationService.generateExplanation(detected);

        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(detected.suggestedActionPayload());
        } catch (Exception e) {
            log.warn("Failed to serialize suggestedActionPayload: {}", e.getMessage());
            payloadJson = "{}";
        }

        RecommendationPriority priority;
        switch (detected.severity()) {
            case CRITICAL -> priority = RecommendationPriority.CRITICAL;
            case HIGH -> priority = RecommendationPriority.HIGH;
            case MEDIUM -> priority = RecommendationPriority.MEDIUM;
            default -> priority = RecommendationPriority.LOW;
        }

        AiRecommendation rec = new AiRecommendation();
        rec.setTenantId(tenantId);
        rec.setRecommendationNumber(recNumber);
        rec.setSignalId(signal.getId());
        rec.setSignalType(detected.signalType());
        rec.setCategory(detected.category());
        rec.setTitle(detected.defaultTitle());
        rec.setSummary(detected.defaultSummary());
        rec.setDetailedExplanation(explanation.detailedExplanation());
        rec.setWhyImportant(explanation.whyImportant());
        rec.setSourceEntityType(detected.sourceEntityType());
        rec.setSourceEntityId(detected.sourceEntityId());
        rec.setSourceEntityNumber(detected.sourceEntityNumber());
        rec.setPriority(priority);
        rec.setStatus(RecommendationStatus.NEW);
        rec.setGeneratedBy(explanation.generatedBy());
        rec.setEvidenceJson(signal.getEvidenceJson());
        rec.setEvidenceStrength(EvidenceStrength.HIGH);
        rec.setSuggestedActionType(detected.suggestedActionType());
        rec.setSuggestedActionPayloadJson(payloadJson);
        rec.setRuleId(ruleId);
        rec.setCreatedAt(LocalDateTime.now());
        rec.setUpdatedAt(LocalDateTime.now());
        rec.setExpiresAt(LocalDateTime.now().plusDays(7));

        return recommendationRepository.save(rec);
    }

    public Page<AiRecommendation> getRecommendations(String tenantId, RecommendationStatus status, RecommendationPriority priority, BusinessSignalCategory category, Pageable pageable) {
        return recommendationRepository.findFiltered(tenantId, status, priority, category, pageable);
    }

    public Optional<AiRecommendation> getRecommendationById(UUID id, String tenantId) {
        return recommendationRepository.findByIdAndTenantId(id, tenantId);
    }

    public Optional<AiRecommendation> getRecommendationByNumber(String tenantId, String recNumber) {
        return recommendationRepository.findByTenantIdAndRecommendationNumber(tenantId, recNumber);
    }

    @Transactional
    public AiRecommendation markReviewed(UUID id, String tenantId, String reviewedBy) {
        AiRecommendation rec = recommendationRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
        if (rec.getStatus() == RecommendationStatus.NEW) {
            rec.setStatus(RecommendationStatus.REVIEWED);
            rec.setReviewedBy(reviewedBy);
            rec.setReviewedAt(LocalDateTime.now());
            rec.setUpdatedAt(LocalDateTime.now());
            return recommendationRepository.save(rec);
        }
        return rec;
    }

    @Transactional
    public AiRecommendation dismissRecommendation(UUID id, String tenantId, String dismissedBy, String reason) {
        AiRecommendation rec = recommendationRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
        rec.setStatus(RecommendationStatus.DISMISSED);
        rec.setDismissReason(reason);
        rec.setReviewedBy(dismissedBy);
        rec.setReviewedAt(LocalDateTime.now());
        rec.setUpdatedAt(LocalDateTime.now());

        // Also ignore or resolve underlying signal
        if (rec.getSignalId() != null) {
            businessSignalRepository.findById(rec.getSignalId()).ifPresent(s -> {
                s.setStatus(BusinessSignalStatus.IGNORED);
                s.setResolvedAt(LocalDateTime.now());
                businessSignalRepository.save(s);
            });
        }

        return recommendationRepository.save(rec);
    }

    @Transactional
    public void resolveRecommendation(UUID id) {
        recommendationRepository.findById(id).ifPresent(rec -> {
            rec.setStatus(RecommendationStatus.RESOLVED);
            rec.setResolvedAt(LocalDateTime.now());
            rec.setUpdatedAt(LocalDateTime.now());
            recommendationRepository.save(rec);

            if (rec.getSignalId() != null) {
                businessSignalRepository.findById(rec.getSignalId()).ifPresent(s -> {
                    s.setStatus(BusinessSignalStatus.RESOLVED);
                    s.setResolvedAt(LocalDateTime.now());
                    businessSignalRepository.save(s);
                });
            }
        });
    }
}
