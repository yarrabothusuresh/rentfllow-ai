package com.rentflow.automation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_recommendations", indexes = {
    @Index(name = "idx_rec_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_rec_tenant_priority", columnList = "tenant_id, priority"),
    @Index(name = "idx_rec_number", columnList = "tenant_id, recommendation_number"),
    @Index(name = "idx_rec_signal", columnList = "signal_id"),
    @Index(name = "idx_rec_created_at", columnList = "created_at")
})
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "recommendation_number", nullable = false)
    private String recommendationNumber;

    @Column(name = "signal_id")
    private UUID signalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false)
    private BusinessSignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private BusinessSignalCategory category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", length = 1000, nullable = false)
    private String summary;

    @Column(name = "detailed_explanation", length = 4000)
    private String detailedExplanation;

    @Column(name = "why_important", length = 2000)
    private String whyImportant;

    @Column(name = "source_entity_type", nullable = false)
    private String sourceEntityType;

    @Column(name = "source_entity_id", nullable = false)
    private String sourceEntityId;

    @Column(name = "source_entity_number")
    private String sourceEntityNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private RecommendationPriority priority = RecommendationPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecommendationStatus status = RecommendationStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "generated_by", nullable = false)
    private RecommendationGeneratedBy generatedBy = RecommendationGeneratedBy.RULE;

    @Column(name = "evidence_json", length = 4000)
    private String evidenceJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_strength", nullable = false)
    private EvidenceStrength evidenceStrength = EvidenceStrength.HIGH;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_action_type")
    private AutomationActionType suggestedActionType;

    @Column(name = "suggested_action_payload_json", length = 4000)
    private String suggestedActionPayloadJson;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "dismiss_reason")
    private String dismissReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public AiRecommendation() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getRecommendationNumber() { return recommendationNumber; }
    public void setRecommendationNumber(String recommendationNumber) { this.recommendationNumber = recommendationNumber; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public BusinessSignalType getSignalType() { return signalType; }
    public void setSignalType(BusinessSignalType signalType) { this.signalType = signalType; }

    public BusinessSignalCategory getCategory() { return category; }
    public void setCategory(BusinessSignalCategory category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDetailedExplanation() { return detailedExplanation; }
    public void setDetailedExplanation(String detailedExplanation) { this.detailedExplanation = detailedExplanation; }

    public String getWhyImportant() { return whyImportant; }
    public void setWhyImportant(String whyImportant) { this.whyImportant = whyImportant; }

    public String getSourceEntityType() { return sourceEntityType; }
    public void setSourceEntityType(String sourceEntityType) { this.sourceEntityType = sourceEntityType; }

    public String getSourceEntityId() { return sourceEntityId; }
    public void setSourceEntityId(String sourceEntityId) { this.sourceEntityId = sourceEntityId; }

    public String getSourceEntityNumber() { return sourceEntityNumber; }
    public void setSourceEntityNumber(String sourceEntityNumber) { this.sourceEntityNumber = sourceEntityNumber; }

    public RecommendationPriority getPriority() { return priority; }
    public void setPriority(RecommendationPriority priority) { this.priority = priority; }

    public RecommendationStatus getStatus() { return status; }
    public void setStatus(RecommendationStatus status) { this.status = status; }

    public RecommendationGeneratedBy getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(RecommendationGeneratedBy generatedBy) { this.generatedBy = generatedBy; }

    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }

    public EvidenceStrength getEvidenceStrength() { return evidenceStrength; }
    public void setEvidenceStrength(EvidenceStrength evidenceStrength) { this.evidenceStrength = evidenceStrength; }

    public AutomationActionType getSuggestedActionType() { return suggestedActionType; }
    public void setSuggestedActionType(AutomationActionType suggestedActionType) { this.suggestedActionType = suggestedActionType; }

    public String getSuggestedActionPayloadJson() { return suggestedActionPayloadJson; }
    public void setSuggestedActionPayloadJson(String suggestedActionPayloadJson) { this.suggestedActionPayloadJson = suggestedActionPayloadJson; }

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getDismissReason() { return dismissReason; }
    public void setDismissReason(String dismissReason) { this.dismissReason = dismissReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
