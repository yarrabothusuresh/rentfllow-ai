package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReturnAndDamageAnalyticsDTO {
    private int totalReturnsProcessed = 0;
    private int totalReturnedUnits = 0;
    private int damagedUnitsCount = 0;
    private BigDecimal damageRatePercent = BigDecimal.ZERO;

    private int damageClaimsCount = 0;
    private BigDecimal estimatedDamageCost = BigDecimal.ZERO;
    private BigDecimal approvedDamageCost = BigDecimal.ZERO;
    private BigDecimal actualRepairCost = BigDecimal.ZERO;
    private BigDecimal replacementCost = BigDecimal.ZERO;
    private BigDecimal lostInventoryCost = BigDecimal.ZERO;

    private int customerApprovedClaimsCount = 0;
    private int disputedClaimsCount = 0;
    private int waivedClaimsCount = 0;

    private BigDecimal averageInspectionMinutes = BigDecimal.ZERO;

    private List<NamedMetricDTO> claimsByStatus = new ArrayList<>();
    private List<NamedMetricDTO> damageByProductCategory = new ArrayList<>();

    public int getTotalReturnsProcessed() { return totalReturnsProcessed; }
    public void setTotalReturnsProcessed(int totalReturnsProcessed) { this.totalReturnsProcessed = totalReturnsProcessed; }

    public int getTotalReturnedUnits() { return totalReturnedUnits; }
    public void setTotalReturnedUnits(int totalReturnedUnits) { this.totalReturnedUnits = totalReturnedUnits; }

    public int getDamagedUnitsCount() { return damagedUnitsCount; }
    public void setDamagedUnitsCount(int damagedUnitsCount) { this.damagedUnitsCount = damagedUnitsCount; }

    public BigDecimal getDamageRatePercent() { return damageRatePercent; }
    public void setDamageRatePercent(BigDecimal damageRatePercent) { this.damageRatePercent = damageRatePercent; }

    public int getDamageClaimsCount() { return damageClaimsCount; }
    public void setDamageClaimsCount(int damageClaimsCount) { this.damageClaimsCount = damageClaimsCount; }

    public BigDecimal getEstimatedDamageCost() { return estimatedDamageCost; }
    public void setEstimatedDamageCost(BigDecimal estimatedDamageCost) { this.estimatedDamageCost = estimatedDamageCost; }

    public BigDecimal getApprovedDamageCost() { return approvedDamageCost; }
    public void setApprovedDamageCost(BigDecimal approvedDamageCost) { this.approvedDamageCost = approvedDamageCost; }

    public BigDecimal getActualRepairCost() { return actualRepairCost; }
    public void setActualRepairCost(BigDecimal actualRepairCost) { this.actualRepairCost = actualRepairCost; }

    public BigDecimal getReplacementCost() { return replacementCost; }
    public void setReplacementCost(BigDecimal replacementCost) { this.replacementCost = replacementCost; }

    public BigDecimal getLostInventoryCost() { return lostInventoryCost; }
    public void setLostInventoryCost(BigDecimal lostInventoryCost) { this.lostInventoryCost = lostInventoryCost; }

    public int getCustomerApprovedClaimsCount() { return customerApprovedClaimsCount; }
    public void setCustomerApprovedClaimsCount(int customerApprovedClaimsCount) { this.customerApprovedClaimsCount = customerApprovedClaimsCount; }

    public int getDisputedClaimsCount() { return disputedClaimsCount; }
    public void setDisputedClaimsCount(int disputedClaimsCount) { this.disputedClaimsCount = disputedClaimsCount; }

    public int getWaivedClaimsCount() { return waivedClaimsCount; }
    public void setWaivedClaimsCount(int waivedClaimsCount) { this.waivedClaimsCount = waivedClaimsCount; }

    public BigDecimal getAverageInspectionMinutes() { return averageInspectionMinutes; }
    public void setAverageInspectionMinutes(BigDecimal averageInspectionMinutes) { this.averageInspectionMinutes = averageInspectionMinutes; }

    public List<NamedMetricDTO> getClaimsByStatus() { return claimsByStatus; }
    public void setClaimsByStatus(List<NamedMetricDTO> claimsByStatus) { this.claimsByStatus = claimsByStatus; }

    public List<NamedMetricDTO> getDamageByProductCategory() { return damageByProductCategory; }
    public void setDamageByProductCategory(List<NamedMetricDTO> damageByProductCategory) { this.damageByProductCategory = damageByProductCategory; }
}
