package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BusinessInsightDTO {
    private String insightKey; // e.g. "HIGH_DEMAND_EXPANSION_OPPORTUNITY", "LOSS_MAKING_BOOKING", "UNDERUTILIZED_ASSETS"
    private String title;
    private String summary;
    private String category; // REVENUE, PROFITABILITY, INVENTORY, SALES, OPERATIONS
    private String severity; // INFO, WARNING, CRITICAL, OPPORTUNITY
    private BigDecimal financialImpact = BigDecimal.ZERO;
    private Map<String, Object> contextData = new HashMap<>();

    public BusinessInsightDTO() {}

    public BusinessInsightDTO(String insightKey, String title, String summary, String category, String severity, BigDecimal financialImpact) {
        this.insightKey = insightKey;
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.severity = severity;
        this.financialImpact = financialImpact;
    }

    public String getInsightKey() { return insightKey; }
    public void setInsightKey(String insightKey) { this.insightKey = insightKey; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public BigDecimal getFinancialImpact() { return financialImpact; }
    public void setFinancialImpact(BigDecimal financialImpact) { this.financialImpact = financialImpact; }

    public Map<String, Object> getContextData() { return contextData; }
    public void setContextData(Map<String, Object> contextData) { this.contextData = contextData; }
}
