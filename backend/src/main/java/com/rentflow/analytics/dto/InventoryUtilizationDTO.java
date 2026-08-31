package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InventoryUtilizationDTO {
    private BigDecimal overallUtilizationPercent = BigDecimal.ZERO;
    private BigDecimal targetUtilizationPercent = new BigDecimal("70.0");
    private int totalTrackedProducts = 0;
    private int totalFleetUnits = 0;
    private int unitsOnRent = 0;
    private int unitsInMaintenance = 0;
    private int unitsDamaged = 0;

    private List<CategoryBreakdownDTO> utilizationByCategory = new ArrayList<>();
    private List<ProductProfitabilityDTO> topUtilizedProducts = new ArrayList<>();
    private List<UnderutilizedProductDTO> underutilizedProducts = new ArrayList<>();
    private List<ConflictDemandProductDTO> highDemandConflictProducts = new ArrayList<>();

    public BigDecimal getOverallUtilizationPercent() { return overallUtilizationPercent; }
    public void setOverallUtilizationPercent(BigDecimal overallUtilizationPercent) { this.overallUtilizationPercent = overallUtilizationPercent; }

    public BigDecimal getTargetUtilizationPercent() { return targetUtilizationPercent; }
    public void setTargetUtilizationPercent(BigDecimal targetUtilizationPercent) { this.targetUtilizationPercent = targetUtilizationPercent; }

    public int getTotalTrackedProducts() { return totalTrackedProducts; }
    public void setTotalTrackedProducts(int totalTrackedProducts) { this.totalTrackedProducts = totalTrackedProducts; }

    public int getTotalFleetUnits() { return totalFleetUnits; }
    public void setTotalFleetUnits(int totalFleetUnits) { this.totalFleetUnits = totalFleetUnits; }

    public int getUnitsOnRent() { return unitsOnRent; }
    public void setUnitsOnRent(int unitsOnRent) { this.unitsOnRent = unitsOnRent; }

    public int getUnitsInMaintenance() { return unitsInMaintenance; }
    public void setUnitsInMaintenance(int unitsInMaintenance) { this.unitsInMaintenance = unitsInMaintenance; }

    public int getUnitsDamaged() { return unitsDamaged; }
    public void setUnitsDamaged(int unitsDamaged) { this.unitsDamaged = unitsDamaged; }

    public List<CategoryBreakdownDTO> getUtilizationByCategory() { return utilizationByCategory; }
    public void setUtilizationByCategory(List<CategoryBreakdownDTO> utilizationByCategory) { this.utilizationByCategory = utilizationByCategory; }

    public List<ProductProfitabilityDTO> getTopUtilizedProducts() { return topUtilizedProducts; }
    public void setTopUtilizedProducts(List<ProductProfitabilityDTO> topUtilizedProducts) { this.topUtilizedProducts = topUtilizedProducts; }

    public List<UnderutilizedProductDTO> getUnderutilizedProducts() { return underutilizedProducts; }
    public void setUnderutilizedProducts(List<UnderutilizedProductDTO> underutilizedProducts) { this.underutilizedProducts = underutilizedProducts; }

    public List<ConflictDemandProductDTO> getHighDemandConflictProducts() { return highDemandConflictProducts; }
    public void setHighDemandConflictProducts(List<ConflictDemandProductDTO> highDemandConflictProducts) { this.highDemandConflictProducts = highDemandConflictProducts; }
}
