package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RevenueAnalyticsDTO {
    private String currency = "USD";
    private BigDecimal totalBookedRevenue = BigDecimal.ZERO;
    private BigDecimal totalInvoicedRevenue = BigDecimal.ZERO;
    private BigDecimal totalCollectedRevenue = BigDecimal.ZERO;
    private BigDecimal totalOutstandingRevenue = BigDecimal.ZERO;
    private BigDecimal averageBookingValue = BigDecimal.ZERO;
    private BigDecimal revenueGrowthPercent = BigDecimal.ZERO;

    // Segment breakdowns
    private List<RevenueTrendItemDTO> revenueTrend = new ArrayList<>();
    private List<CategoryBreakdownDTO> revenueByCategory = new ArrayList<>();
    private List<NamedMetricDTO> revenueByEventType = new ArrayList<>();
    private List<NamedMetricDTO> revenueByCustomer = new ArrayList<>();
    private List<NamedMetricDTO> revenueByBookingStatus = new ArrayList<>();

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTotalBookedRevenue() { return totalBookedRevenue; }
    public void setTotalBookedRevenue(BigDecimal totalBookedRevenue) { this.totalBookedRevenue = totalBookedRevenue; }

    public BigDecimal getTotalInvoicedRevenue() { return totalInvoicedRevenue; }
    public void setTotalInvoicedRevenue(BigDecimal totalInvoicedRevenue) { this.totalInvoicedRevenue = totalInvoicedRevenue; }

    public BigDecimal getTotalCollectedRevenue() { return totalCollectedRevenue; }
    public void setTotalCollectedRevenue(BigDecimal totalCollectedRevenue) { this.totalCollectedRevenue = totalCollectedRevenue; }

    public BigDecimal getTotalOutstandingRevenue() { return totalOutstandingRevenue; }
    public void setTotalOutstandingRevenue(BigDecimal totalOutstandingRevenue) { this.totalOutstandingRevenue = totalOutstandingRevenue; }

    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public void setAverageBookingValue(BigDecimal averageBookingValue) { this.averageBookingValue = averageBookingValue; }

    public BigDecimal getRevenueGrowthPercent() { return revenueGrowthPercent; }
    public void setRevenueGrowthPercent(BigDecimal revenueGrowthPercent) { this.revenueGrowthPercent = revenueGrowthPercent; }

    public List<RevenueTrendItemDTO> getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(List<RevenueTrendItemDTO> revenueTrend) { this.revenueTrend = revenueTrend; }

    public List<CategoryBreakdownDTO> getRevenueByCategory() { return revenueByCategory; }
    public void setRevenueByCategory(List<CategoryBreakdownDTO> revenueByCategory) { this.revenueByCategory = revenueByCategory; }

    public List<NamedMetricDTO> getRevenueByEventType() { return revenueByEventType; }
    public void setRevenueByEventType(List<NamedMetricDTO> revenueByEventType) { this.revenueByEventType = revenueByEventType; }

    public List<NamedMetricDTO> getRevenueByCustomer() { return revenueByCustomer; }
    public void setRevenueByCustomer(List<NamedMetricDTO> revenueByCustomer) { this.revenueByCustomer = revenueByCustomer; }

    public List<NamedMetricDTO> getRevenueByBookingStatus() { return revenueByBookingStatus; }
    public void setRevenueByBookingStatus(List<NamedMetricDTO> revenueByBookingStatus) { this.revenueByBookingStatus = revenueByBookingStatus; }
}
