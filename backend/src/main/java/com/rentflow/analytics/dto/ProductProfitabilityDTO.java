package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductProfitabilityDTO {
    private UUID productId;
    private String sku;
    private String name;
    private String categoryName;
    private int quantityOwned = 0;

    private BigDecimal rentalRevenue = BigDecimal.ZERO;
    private int rentalCount = 0;
    private int totalQuantityRented = 0;
    private BigDecimal utilizationPercent = BigDecimal.ZERO;

    private BigDecimal directCost = BigDecimal.ZERO;
    private BigDecimal repairCost = BigDecimal.ZERO;
    private BigDecimal damageCost = BigDecimal.ZERO;
    private BigDecimal replacementLossCost = BigDecimal.ZERO;

    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal marginPercent = BigDecimal.ZERO;
    private String marginFlag; // HIGH_MARGIN, HEALTHY_MARGIN, LOW_MARGIN, NEGATIVE_MARGIN

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getQuantityOwned() { return quantityOwned; }
    public void setQuantityOwned(int quantityOwned) { this.quantityOwned = quantityOwned; }

    public BigDecimal getRentalRevenue() { return rentalRevenue; }
    public void setRentalRevenue(BigDecimal rentalRevenue) { this.rentalRevenue = rentalRevenue; }

    public int getRentalCount() { return rentalCount; }
    public void setRentalCount(int rentalCount) { this.rentalCount = rentalCount; }

    public int getTotalQuantityRented() { return totalQuantityRented; }
    public void setTotalQuantityRented(int totalQuantityRented) { this.totalQuantityRented = totalQuantityRented; }

    public BigDecimal getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(BigDecimal utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public BigDecimal getDirectCost() { return directCost; }
    public void setDirectCost(BigDecimal directCost) { this.directCost = directCost; }

    public BigDecimal getRepairCost() { return repairCost; }
    public void setRepairCost(BigDecimal repairCost) { this.repairCost = repairCost; }

    public BigDecimal getDamageCost() { return damageCost; }
    public void setDamageCost(BigDecimal damageCost) { this.damageCost = damageCost; }

    public BigDecimal getReplacementLossCost() { return replacementLossCost; }
    public void setReplacementLossCost(BigDecimal replacementLossCost) { this.replacementLossCost = replacementLossCost; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }

    public BigDecimal getMarginPercent() { return marginPercent; }
    public void setMarginPercent(BigDecimal marginPercent) { this.marginPercent = marginPercent; }

    public String getMarginFlag() { return marginFlag; }
    public void setMarginFlag(String marginFlag) { this.marginFlag = marginFlag; }
}
