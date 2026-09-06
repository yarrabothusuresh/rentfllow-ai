package com.rentflow.automation.dto;

import com.rentflow.automation.model.AiRecommendation;
import com.rentflow.automation.model.AutomationApproval;
import com.rentflow.automation.model.AutomationExecution;

import java.util.List;

public class AutomationDashboardDTO {
    private long totalActiveRecommendations;
    private long criticalRecommendations;
    private long highRecommendations;
    private long mediumRecommendations;
    private long lowRecommendations;
    private long pendingApprovals;
    private long totalSignalsActive;
    private long totalExecutions;
    private List<AiRecommendation> recentRecommendations;
    private List<AutomationApproval> pendingApprovalsList;
    private List<AutomationExecution> recentExecutions;

    public AutomationDashboardDTO() {}

    public long getTotalActiveRecommendations() { return totalActiveRecommendations; }
    public void setTotalActiveRecommendations(long totalActiveRecommendations) { this.totalActiveRecommendations = totalActiveRecommendations; }

    public long getCriticalRecommendations() { return criticalRecommendations; }
    public void setCriticalRecommendations(long criticalRecommendations) { this.criticalRecommendations = criticalRecommendations; }

    public long getHighRecommendations() { return highRecommendations; }
    public void setHighRecommendations(long highRecommendations) { this.highRecommendations = highRecommendations; }

    public long getMediumRecommendations() { return mediumRecommendations; }
    public void setMediumRecommendations(long mediumRecommendations) { this.mediumRecommendations = mediumRecommendations; }

    public long getLowRecommendations() { return lowRecommendations; }
    public void setLowRecommendations(long lowRecommendations) { this.lowRecommendations = lowRecommendations; }

    public long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(long pendingApprovals) { this.pendingApprovals = pendingApprovals; }

    public long getTotalSignalsActive() { return totalSignalsActive; }
    public void setTotalSignalsActive(long totalSignalsActive) { this.totalSignalsActive = totalSignalsActive; }

    public long getTotalExecutions() { return totalExecutions; }
    public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }

    public List<AiRecommendation> getRecentRecommendations() { return recentRecommendations; }
    public void setRecentRecommendations(List<AiRecommendation> recentRecommendations) { this.recentRecommendations = recentRecommendations; }

    public List<AutomationApproval> getPendingApprovalsList() { return pendingApprovalsList; }
    public void setPendingApprovalsList(List<AutomationApproval> pendingApprovalsList) { this.pendingApprovalsList = pendingApprovalsList; }

    public List<AutomationExecution> getRecentExecutions() { return recentExecutions; }
    public void setRecentExecutions(List<AutomationExecution> recentExecutions) { this.recentExecutions = recentExecutions; }
}
