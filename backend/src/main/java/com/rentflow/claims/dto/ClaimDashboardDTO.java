package com.rentflow.claims.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClaimDashboardDTO {
    private long openClaims;
    private long underReview;
    private long customerReview;
    private long approved;
    private long disputed;
    private long repairInProgress;
    private long replacementRequired;
    private long resolved;
    private BigDecimal estimatedExposure = BigDecimal.ZERO;
    private BigDecimal approvedTotal = BigDecimal.ZERO;
    private BigDecimal resolvedTotal = BigDecimal.ZERO;
    private double damageRate;
    private double missingRate;
    private String currency = "USD";

    private List<DamageClaimDTO> recentClaims = new ArrayList<>();

    public long getOpenClaims() { return openClaims; }
    public void setOpenClaims(long openClaims) { this.openClaims = openClaims; }

    public long getUnderReview() { return underReview; }
    public void setUnderReview(long underReview) { this.underReview = underReview; }

    public long getCustomerReview() { return customerReview; }
    public void setCustomerReview(long customerReview) { this.customerReview = customerReview; }

    public long getApproved() { return approved; }
    public void setApproved(long approved) { this.approved = approved; }

    public long getDisputed() { return disputed; }
    public void setDisputed(long disputed) { this.disputed = disputed; }

    public long getRepairInProgress() { return repairInProgress; }
    public void setRepairInProgress(long repairInProgress) { this.repairInProgress = repairInProgress; }

    public long getReplacementRequired() { return replacementRequired; }
    public void setReplacementRequired(long replacementRequired) { this.replacementRequired = replacementRequired; }

    public long getResolved() { return resolved; }
    public void setResolved(long resolved) { this.resolved = resolved; }

    public BigDecimal getEstimatedExposure() { return estimatedExposure; }
    public void setEstimatedExposure(BigDecimal estimatedExposure) { this.estimatedExposure = estimatedExposure; }

    public BigDecimal getApprovedTotal() { return approvedTotal; }
    public void setApprovedTotal(BigDecimal approvedTotal) { this.approvedTotal = approvedTotal; }

    public BigDecimal getResolvedTotal() { return resolvedTotal; }
    public void setResolvedTotal(BigDecimal resolvedTotal) { this.resolvedTotal = resolvedTotal; }

    public double getDamageRate() { return damageRate; }
    public void setDamageRate(double damageRate) { this.damageRate = damageRate; }

    public double getMissingRate() { return missingRate; }
    public void setMissingRate(double missingRate) { this.missingRate = missingRate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<DamageClaimDTO> getRecentClaims() { return recentClaims; }
    public void setRecentClaims(List<DamageClaimDTO> recentClaims) { this.recentClaims = recentClaims; }
}
