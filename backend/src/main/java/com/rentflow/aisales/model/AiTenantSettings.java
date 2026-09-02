package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_tenant_settings", indexes = {
    @Index(name = "idx_aisettings_tenant", columnList = "tenantId", unique = true)
})
public class AiTenantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tenantId;

    @Column(nullable = false)
    private boolean aiEnabled = true;

    @Column(nullable = false)
    private String aiProvider = "mock"; // mock, openai, gemini

    @Column(nullable = false)
    private String aiModel = "mock-sales-v1";

    @Column(nullable = false)
    private boolean customerAiEnabled = true;

    @Column(nullable = false)
    private boolean internalSalesAssistantEnabled = true;

    @Column(nullable = false)
    private boolean humanQuoteApprovalRequired = true;

    @Column(nullable = false)
    private int dailyRequestLimit = 500;

    @Column(nullable = false)
    private int maxConversationMessages = 20;

    @Column(nullable = false)
    private double targetGrossMarginPct = 30.0;

    @Column(nullable = false)
    private double lowMarginThresholdPct = 20.0;

    private String apiKeyMasked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public boolean isAiEnabled() { return aiEnabled; }
    public void setAiEnabled(boolean aiEnabled) { this.aiEnabled = aiEnabled; }

    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public boolean isCustomerAiEnabled() { return customerAiEnabled; }
    public void setCustomerAiEnabled(boolean customerAiEnabled) { this.customerAiEnabled = customerAiEnabled; }

    public boolean isInternalSalesAssistantEnabled() { return internalSalesAssistantEnabled; }
    public void setInternalSalesAssistantEnabled(boolean internalSalesAssistantEnabled) { this.internalSalesAssistantEnabled = internalSalesAssistantEnabled; }

    public boolean isHumanQuoteApprovalRequired() { return humanQuoteApprovalRequired; }
    public void setHumanQuoteApprovalRequired(boolean humanQuoteApprovalRequired) { this.humanQuoteApprovalRequired = humanQuoteApprovalRequired; }

    public int getDailyRequestLimit() { return dailyRequestLimit; }
    public void setDailyRequestLimit(int dailyRequestLimit) { this.dailyRequestLimit = dailyRequestLimit; }

    public int getMaxConversationMessages() { return maxConversationMessages; }
    public void setMaxConversationMessages(int maxConversationMessages) { this.maxConversationMessages = maxConversationMessages; }

    public double getTargetGrossMarginPct() { return targetGrossMarginPct; }
    public void setTargetGrossMarginPct(double targetGrossMarginPct) { this.targetGrossMarginPct = targetGrossMarginPct; }

    public double getLowMarginThresholdPct() { return lowMarginThresholdPct; }
    public void setLowMarginThresholdPct(double lowMarginThresholdPct) { this.lowMarginThresholdPct = lowMarginThresholdPct; }

    public String getApiKeyMasked() { return apiKeyMasked; }
    public void setApiKeyMasked(String apiKeyMasked) { this.apiKeyMasked = apiKeyMasked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
