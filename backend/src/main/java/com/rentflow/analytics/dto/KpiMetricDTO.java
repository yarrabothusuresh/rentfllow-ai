package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class KpiMetricDTO {
    private String name;
    private BigDecimal value;
    private String formattedValue;
    private String unit; // CURRENCY, PERCENT, COUNT, TIME_MINUTES
    private BigDecimal previousValue;
    private BigDecimal changePercent;
    private String trend; // UP, DOWN, FLAT
    private String status; // NORMAL, WARNING, DANGER, SUCCESS

    public KpiMetricDTO() {}

    public KpiMetricDTO(String name, BigDecimal value, String formattedValue, String unit, BigDecimal previousValue, BigDecimal changePercent, String trend, String status) {
        this.name = name;
        this.value = value;
        this.formattedValue = formattedValue;
        this.unit = unit;
        this.previousValue = previousValue;
        this.changePercent = changePercent;
        this.trend = trend;
        this.status = status;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getValue() { return value; }
    public BigDecimal getCurrentValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public String getFormattedValue() { return formattedValue; }
    public void setFormattedValue(String formattedValue) { this.formattedValue = formattedValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPreviousValue() { return previousValue; }
    public void setPreviousValue(BigDecimal previousValue) { this.previousValue = previousValue; }

    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
