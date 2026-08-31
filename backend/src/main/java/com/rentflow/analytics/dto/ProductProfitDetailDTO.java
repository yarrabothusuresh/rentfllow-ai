package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductProfitDetailDTO {
    private UUID productId;
    private String sku;
    private String name;
    private String categoryName;
    private String trackingType;
    private int quantityOwned = 0;
    private int quantityInMaintenance = 0;
    private int quantityDamaged = 0;
    private int quantityLost = 0;

    private BigDecimal rentalPrice = BigDecimal.ZERO;
    private BigDecimal replacementCost = BigDecimal.ZERO;

    // Performance metrics
    private BigDecimal rentalRevenue = BigDecimal.ZERO;
    private int bookingCount = 0;
    private int totalQuantityRented = 0;
    private BigDecimal utilizationPercent = BigDecimal.ZERO;
    private BigDecimal averageRentalPrice = BigDecimal.ZERO;

    // Costs
    private BigDecimal directProductCost = BigDecimal.ZERO;
    private BigDecimal repairCost = BigDecimal.ZERO;
    private BigDecimal damageCost = BigDecimal.ZERO;
    private BigDecimal replacementLossCost = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;

    // ROI & Profit
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal marginPercent = BigDecimal.ZERO;
    private String marginFlag;

    // Time-series trend
    private List<RevenueTrendItemDTO> monthlyTrend = new ArrayList<>();
    private List<CostSourceExplanationDTO> costSources = new ArrayList<>();

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getTrackingType() { return trackingType; }
    public void setTrackingType(String trackingType) { this.trackingType = trackingType; }

    public int getQuantityOwned() { return quantityOwned; }
    public void setQuantityOwned(int quantityOwned) { this.quantityOwned = quantityOwned; }

    public int getQuantityInMaintenance() { return quantityInMaintenance; }
    public void setQuantityInMaintenance(int quantityInMaintenance) { this.quantityInMaintenance = quantityInMaintenance; }

    public int getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(int quantityDamaged) { this.quantityDamaged = quantityDamaged; }

    public int getQuantityLost() { return quantityLost; }
    public void setQuantityLost(int quantityLost) { this.quantityLost = quantityLost; }

    public BigDecimal getRentalPrice() { return rentalPrice; }
    public void setRentalPrice(BigDecimal rentalPrice) { this.rentalPrice = rentalPrice; }

    public BigDecimal getReplacementCost() { return replacementCost; }
    public void setReplacementCost(BigDecimal replacementCost) { this.replacementCost = replacementCost; }

    public BigDecimal getRentalRevenue() { return rentalRevenue; }
    public void setRentalRevenue(BigDecimal rentalRevenue) { this.rentalRevenue = rentalRevenue; }

    public int getBookingCount() { return bookingCount; }
    public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }

    public int getTotalQuantityRented() { return totalQuantityRented; }
    public void setTotalQuantityRented(int totalQuantityRented) { this.totalQuantityRented = totalQuantityRented; }

    public BigDecimal getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(BigDecimal utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public BigDecimal getAverageRentalPrice() { return averageRentalPrice; }
    public void setAverageRentalPrice(BigDecimal averageRentalPrice) { this.averageRentalPrice = averageRentalPrice; }

    public BigDecimal getDirectProductCost() { return directProductCost; }
    public void setDirectProductCost(BigDecimal directProductCost) { this.directProductCost = directProductCost; }

    public BigDecimal getRepairCost() { return repairCost; }
    public void setRepairCost(BigDecimal repairCost) { this.repairCost = repairCost; }

    public BigDecimal getDamageCost() { return damageCost; }
    public void setDamageCost(BigDecimal damageCost) { this.damageCost = damageCost; }

    public BigDecimal getReplacementLossCost() { return replacementLossCost; }
    public void setReplacementLossCost(BigDecimal replacementLossCost) { this.replacementLossCost = replacementLossCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }

    public BigDecimal getMarginPercent() { return marginPercent; }
    public void setMarginPercent(BigDecimal marginPercent) { this.marginPercent = marginPercent; }

    public String getMarginFlag() { return marginFlag; }
    public void setMarginFlag(String marginFlag) { this.marginFlag = marginFlag; }

    public List<RevenueTrendItemDTO> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<RevenueTrendItemDTO> monthlyTrend) { this.monthlyTrend = monthlyTrend; }

    public List<CostSourceExplanationDTO> getCostSources() { return costSources; }
    public void setCostSources(List<CostSourceExplanationDTO> costSources) { this.costSources = costSources; }
}
