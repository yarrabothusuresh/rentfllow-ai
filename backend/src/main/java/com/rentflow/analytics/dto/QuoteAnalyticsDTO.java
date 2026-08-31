package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class QuoteAnalyticsDTO {
    private int totalQuotesCreated = 0;
    private int quotesSent = 0;
    private int quotesApproved = 0;
    private int quotesDeclined = 0;
    private int quotesExpired = 0;

    private BigDecimal overallConversionRate = BigDecimal.ZERO;
    private BigDecimal totalQuotedValue = BigDecimal.ZERO;
    private BigDecimal totalApprovedValue = BigDecimal.ZERO;
    private BigDecimal averageQuoteValue = BigDecimal.ZERO;
    private BigDecimal averageApprovalHours = BigDecimal.ZERO;

    private BigDecimal potentialLostRevenue = BigDecimal.ZERO; // From declined + expired quotes

    private List<SalesFunnelStageDTO> salesFunnel = new ArrayList<>();
    private List<NamedMetricDTO> quotesByStatus = new ArrayList<>();

    public int getTotalQuotesCreated() { return totalQuotesCreated; }
    public void setTotalQuotesCreated(int totalQuotesCreated) { this.totalQuotesCreated = totalQuotesCreated; }

    public int getQuotesSent() { return quotesSent; }
    public void setQuotesSent(int quotesSent) { this.quotesSent = quotesSent; }

    public int getQuotesApproved() { return quotesApproved; }
    public void setQuotesApproved(int quotesApproved) { this.quotesApproved = quotesApproved; }

    public int getQuotesDeclined() { return quotesDeclined; }
    public void setQuotesDeclined(int quotesDeclined) { this.quotesDeclined = quotesDeclined; }

    public int getQuotesExpired() { return quotesExpired; }
    public void setQuotesExpired(int quotesExpired) { this.quotesExpired = quotesExpired; }

    public BigDecimal getOverallConversionRate() { return overallConversionRate; }
    public void setOverallConversionRate(BigDecimal overallConversionRate) { this.overallConversionRate = overallConversionRate; }

    public BigDecimal getTotalQuotedValue() { return totalQuotedValue; }
    public void setTotalQuotedValue(BigDecimal totalQuotedValue) { this.totalQuotedValue = totalQuotedValue; }

    public BigDecimal getTotalApprovedValue() { return totalApprovedValue; }
    public void setTotalApprovedValue(BigDecimal totalApprovedValue) { this.totalApprovedValue = totalApprovedValue; }

    public BigDecimal getAverageQuoteValue() { return averageQuoteValue; }
    public void setAverageQuoteValue(BigDecimal averageQuoteValue) { this.averageQuoteValue = averageQuoteValue; }

    public BigDecimal getAverageApprovalHours() { return averageApprovalHours; }
    public void setAverageApprovalHours(BigDecimal averageApprovalHours) { this.averageApprovalHours = averageApprovalHours; }

    public BigDecimal getPotentialLostRevenue() { return potentialLostRevenue; }
    public void setPotentialLostRevenue(BigDecimal potentialLostRevenue) { this.potentialLostRevenue = potentialLostRevenue; }

    public List<SalesFunnelStageDTO> getSalesFunnel() { return salesFunnel; }
    public void setSalesFunnel(List<SalesFunnelStageDTO> salesFunnel) { this.salesFunnel = salesFunnel; }

    public List<NamedMetricDTO> getQuotesByStatus() { return quotesByStatus; }
    public void setQuotesByStatus(List<NamedMetricDTO> quotesByStatus) { this.quotesByStatus = quotesByStatus; }
}
