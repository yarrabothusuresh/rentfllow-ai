package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExecutiveDashboardDTO {
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String currency;
    private LocalDateTime lastUpdated;
    private String dataFreshness; // REAL_TIME, NEAR_REAL_TIME, SNAPSHOT
    private List<String> dataQualityWarnings = new ArrayList<>();

    // Top KPI Cards
    private KpiMetricDTO bookedRevenue;
    private KpiMetricDTO collectedRevenue;
    private KpiMetricDTO outstandingRevenue;
    private KpiMetricDTO grossProfit;
    private KpiMetricDTO grossMargin;
    private KpiMetricDTO bookingsCount;
    private KpiMetricDTO averageBookingValue;
    private KpiMetricDTO inventoryUtilization;
    private KpiMetricDTO quoteConversionRate;
    private KpiMetricDTO openDamageExposure;

    // Charts data
    private List<RevenueTrendItemDTO> revenueTrend = new ArrayList<>();
    private List<CategoryBreakdownDTO> revenueByCategory = new ArrayList<>();
    private List<ProductProfitabilityDTO> topProfitableProducts = new ArrayList<>();
    private List<CustomerAnalyticsDTO> topCustomers = new ArrayList<>();

    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getDataFreshness() { return dataFreshness; }
    public void setDataFreshness(String dataFreshness) { this.dataFreshness = dataFreshness; }

    public List<String> getDataQualityWarnings() { return dataQualityWarnings; }
    public void setDataQualityWarnings(List<String> dataQualityWarnings) { this.dataQualityWarnings = dataQualityWarnings; }

    public KpiMetricDTO getBookedRevenue() { return bookedRevenue; }
    public void setBookedRevenue(KpiMetricDTO bookedRevenue) { this.bookedRevenue = bookedRevenue; }

    public KpiMetricDTO getCollectedRevenue() { return collectedRevenue; }
    public void setCollectedRevenue(KpiMetricDTO collectedRevenue) { this.collectedRevenue = collectedRevenue; }

    public KpiMetricDTO getOutstandingRevenue() { return outstandingRevenue; }
    public void setOutstandingRevenue(KpiMetricDTO outstandingRevenue) { this.outstandingRevenue = outstandingRevenue; }

    public KpiMetricDTO getGrossProfit() { return grossProfit; }
    public void setGrossProfit(KpiMetricDTO grossProfit) { this.grossProfit = grossProfit; }

    public KpiMetricDTO getGrossMargin() { return grossMargin; }
    public void setGrossMargin(KpiMetricDTO grossMargin) { this.grossMargin = grossMargin; }

    public KpiMetricDTO getBookingsCount() { return bookingsCount; }
    public void setBookingsCount(KpiMetricDTO bookingsCount) { this.bookingsCount = bookingsCount; }

    public KpiMetricDTO getAverageBookingValue() { return averageBookingValue; }
    public void setAverageBookingValue(KpiMetricDTO averageBookingValue) { this.averageBookingValue = averageBookingValue; }

    public KpiMetricDTO getInventoryUtilization() { return inventoryUtilization; }
    public void setInventoryUtilization(KpiMetricDTO inventoryUtilization) { this.inventoryUtilization = inventoryUtilization; }

    public KpiMetricDTO getQuoteConversionRate() { return quoteConversionRate; }
    public void setQuoteConversionRate(KpiMetricDTO quoteConversionRate) { this.quoteConversionRate = quoteConversionRate; }

    public KpiMetricDTO getOpenDamageExposure() { return openDamageExposure; }
    public void setOpenDamageExposure(KpiMetricDTO openDamageExposure) { this.openDamageExposure = openDamageExposure; }

    public List<RevenueTrendItemDTO> getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(List<RevenueTrendItemDTO> revenueTrend) { this.revenueTrend = revenueTrend; }

    public List<CategoryBreakdownDTO> getRevenueByCategory() { return revenueByCategory; }
    public void setRevenueByCategory(List<CategoryBreakdownDTO> revenueByCategory) { this.revenueByCategory = revenueByCategory; }

    public List<ProductProfitabilityDTO> getTopProfitableProducts() { return topProfitableProducts; }
    public void setTopProfitableProducts(List<ProductProfitabilityDTO> topProfitableProducts) { this.topProfitableProducts = topProfitableProducts; }

    public List<CustomerAnalyticsDTO> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<CustomerAnalyticsDTO> topCustomers) { this.topCustomers = topCustomers; }
}
