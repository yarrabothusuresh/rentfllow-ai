package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class NamedMetricDTO {
    private String name;
    private BigDecimal value = BigDecimal.ZERO;
    private int count = 0;
    private BigDecimal percentage = BigDecimal.ZERO;

    public NamedMetricDTO() {}

    public NamedMetricDTO(String name, BigDecimal value, int count, BigDecimal percentage) {
        this.name = name;
        this.value = value;
        this.count = count;
        this.percentage = percentage;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
