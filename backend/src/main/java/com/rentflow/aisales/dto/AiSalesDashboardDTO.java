package com.rentflow.aisales.dto;

import java.util.ArrayList;
import java.util.List;

public class AiSalesDashboardDTO {
    private long activeConversations;
    private long newInquiriesToday;
    private long waitingForCustomer;
    private long waitingForHuman;
    private long quoteDraftsPendingReview;
    private long openEscalations;
    private long convertedConversations;
    private double conversionRate;
    private long tokensUsedToday;
    private List<AiSalesConversationDTO> recentConversations = new ArrayList<>();
    private List<AiEscalationDTO> urgentEscalations = new ArrayList<>();

    public AiSalesDashboardDTO() {}

    public long getActiveConversations() { return activeConversations; }
    public void setActiveConversations(long activeConversations) { this.activeConversations = activeConversations; }

    public long getNewInquiriesToday() { return newInquiriesToday; }
    public void setNewInquiriesToday(long newInquiriesToday) { this.newInquiriesToday = newInquiriesToday; }

    public long getWaitingForCustomer() { return waitingForCustomer; }
    public void setWaitingForCustomer(long waitingForCustomer) { this.waitingForCustomer = waitingForCustomer; }

    public long getWaitingForHuman() { return waitingForHuman; }
    public void setWaitingForHuman(long waitingForHuman) { this.waitingForHuman = waitingForHuman; }

    public long getQuoteDraftsPendingReview() { return quoteDraftsPendingReview; }
    public void setQuoteDraftsPendingReview(long quoteDraftsPendingReview) { this.quoteDraftsPendingReview = quoteDraftsPendingReview; }

    public long getOpenEscalations() { return openEscalations; }
    public void setOpenEscalations(long openEscalations) { this.openEscalations = openEscalations; }

    public long getConvertedConversations() { return convertedConversations; }
    public void setConvertedConversations(long convertedConversations) { this.convertedConversations = convertedConversations; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }

    public long getTokensUsedToday() { return tokensUsedToday; }
    public void setTokensUsedToday(long tokensUsedToday) { this.tokensUsedToday = tokensUsedToday; }

    public List<AiSalesConversationDTO> getRecentConversations() { return recentConversations; }
    public void setRecentConversations(List<AiSalesConversationDTO> recentConversations) { this.recentConversations = recentConversations; }

    public List<AiEscalationDTO> getUrgentEscalations() { return urgentEscalations; }
    public void setUrgentEscalations(List<AiEscalationDTO> urgentEscalations) { this.urgentEscalations = urgentEscalations; }
}
