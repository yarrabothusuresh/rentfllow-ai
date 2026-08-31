package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class WarehouseAnalyticsDTO {
    private int totalOrdersProcessed = 0;
    private int ordersReadyOnTime = 0;
    private BigDecimal onTimeReadinessRate = BigDecimal.ZERO;

    private BigDecimal averagePickDurationMinutes = BigDecimal.ZERO;
    private BigDecimal averagePackDurationMinutes = BigDecimal.ZERO;
    private BigDecimal averageLoadDurationMinutes = BigDecimal.ZERO;
    private BigDecimal averageTotalFulfillmentMinutes = BigDecimal.ZERO;

    private int totalShortPicksCount = 0;
    private int totalDamageFoundInPickCount = 0;
    private int totalExceptionsReported = 0;
    private int resolvedExceptionsCount = 0;

    private List<NamedMetricDTO> exceptionsByType = new ArrayList<>();
    private List<NamedMetricDTO> ordersByStatus = new ArrayList<>();

    public int getTotalOrdersProcessed() { return totalOrdersProcessed; }
    public void setTotalOrdersProcessed(int totalOrdersProcessed) { this.totalOrdersProcessed = totalOrdersProcessed; }

    public int getOrdersReadyOnTime() { return ordersReadyOnTime; }
    public void setOrdersReadyOnTime(int ordersReadyOnTime) { this.ordersReadyOnTime = ordersReadyOnTime; }

    public BigDecimal getOnTimeReadinessRate() { return onTimeReadinessRate; }
    public void setOnTimeReadinessRate(BigDecimal onTimeReadinessRate) { this.onTimeReadinessRate = onTimeReadinessRate; }

    public BigDecimal getAveragePickDurationMinutes() { return averagePickDurationMinutes; }
    public void setAveragePickDurationMinutes(BigDecimal averagePickDurationMinutes) { this.averagePickDurationMinutes = averagePickDurationMinutes; }

    public BigDecimal getAveragePackDurationMinutes() { return averagePackDurationMinutes; }
    public void setAveragePackDurationMinutes(BigDecimal averagePackDurationMinutes) { this.averagePackDurationMinutes = averagePackDurationMinutes; }

    public BigDecimal getAverageLoadDurationMinutes() { return averageLoadDurationMinutes; }
    public void setAverageLoadDurationMinutes(BigDecimal averageLoadDurationMinutes) { this.averageLoadDurationMinutes = averageLoadDurationMinutes; }

    public BigDecimal getAverageTotalFulfillmentMinutes() { return averageTotalFulfillmentMinutes; }
    public void setAverageTotalFulfillmentMinutes(BigDecimal averageTotalFulfillmentMinutes) { this.averageTotalFulfillmentMinutes = averageTotalFulfillmentMinutes; }

    public int getTotalShortPicksCount() { return totalShortPicksCount; }
    public void setTotalShortPicksCount(int totalShortPicksCount) { this.totalShortPicksCount = totalShortPicksCount; }

    public int getTotalDamageFoundInPickCount() { return totalDamageFoundInPickCount; }
    public void setTotalDamageFoundInPickCount(int totalDamageFoundInPickCount) { this.totalDamageFoundInPickCount = totalDamageFoundInPickCount; }

    public int getTotalExceptionsReported() { return totalExceptionsReported; }
    public void setTotalExceptionsReported(int totalExceptionsReported) { this.totalExceptionsReported = totalExceptionsReported; }

    public int getResolvedExceptionsCount() { return resolvedExceptionsCount; }
    public void setResolvedExceptionsCount(int resolvedExceptionsCount) { this.resolvedExceptionsCount = resolvedExceptionsCount; }

    public List<NamedMetricDTO> getExceptionsByType() { return exceptionsByType; }
    public void setExceptionsByType(List<NamedMetricDTO> exceptionsByType) { this.exceptionsByType = exceptionsByType; }

    public List<NamedMetricDTO> getOrdersByStatus() { return ordersByStatus; }
    public void setOrdersByStatus(List<NamedMetricDTO> ordersByStatus) { this.ordersByStatus = ordersByStatus; }
}
