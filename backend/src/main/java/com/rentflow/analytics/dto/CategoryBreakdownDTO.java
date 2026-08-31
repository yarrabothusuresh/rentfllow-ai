package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class CategoryBreakdownDTO {
    private String categoryName;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal marginPercent = BigDecimal.ZERO;
    private int rentalCount = 0;
    private BigDecimal utilizationPercent = BigDecimal.ZERO;

    public CategoryBreakdownDTO() {}

    public CategoryBreakdownDTO(String categoryName, BigDecimal revenue, BigDecimal profit, BigDecimal marginPercent, int rentalCount, BigDecimal utilizationPercent) {
        this.categoryName = categoryName;
        this.revenue = revenue;
        this.profit = profit;
        this.marginPercent = marginPercent;
        this.rentalCount = rentalCount;
        this.utilizationPercent = utilizationPercent;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }

    public BigDecimal getMarginPercent() { return marginPercent; }
    public void setMarginPercent(BigDecimal marginPercent) { this.marginPercent = marginPercent; }

    public int getRentalCount() { return rentalCount; }
    public void setRentalCount(int rentalCount) { this.rentalCount = rentalCount; }

    public BigDecimal getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(BigDecimal utilizationPercent) { this.utilizationPercent = utilizationPercent; }
}
