package com.rentflow.claims.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RepairDashboardDTO {
    private long pendingRepairs;
    private long inProgress;
    private long completed;
    private long failed;
    private BigDecimal estimatedCost = BigDecimal.ZERO;
    private BigDecimal actualCost = BigDecimal.ZERO;

    private List<RepairOrderDTO> recentRepairs = new ArrayList<>();

    public long getPendingRepairs() { return pendingRepairs; }
    public void setPendingRepairs(long pendingRepairs) { this.pendingRepairs = pendingRepairs; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getFailed() { return failed; }
    public void setFailed(long failed) { this.failed = failed; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }

    public List<RepairOrderDTO> getRecentRepairs() { return recentRepairs; }
    public void setRecentRepairs(List<RepairOrderDTO> recentRepairs) { this.recentRepairs = recentRepairs; }
}
