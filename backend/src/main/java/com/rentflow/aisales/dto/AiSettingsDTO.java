package com.rentflow.aisales.dto;

public class AiSettingsDTO {
    private boolean aiEnabled;
    private String aiProvider;
    private String aiModel;
    private boolean customerAiEnabled;
    private boolean internalSalesAssistantEnabled;
    private boolean humanQuoteApprovalRequired;
    private int dailyRequestLimit;
    private int maxConversationMessages;
    private double targetGrossMarginPct;
    private double lowMarginThresholdPct;
    private String apiKeyMasked;

    public AiSettingsDTO() {}

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
}
