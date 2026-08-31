package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class SalesFunnelStageDTO {
    private String stageName; // INQUIRIES, QUOTES, APPROVED, BOOKINGS, COMPLETED
    private int count = 0;
    private BigDecimal value = BigDecimal.ZERO;
    private BigDecimal conversionFromPrevious = BigDecimal.ZERO;
    private BigDecimal overallConversion = BigDecimal.ZERO;

    public SalesFunnelStageDTO() {}

    public SalesFunnelStageDTO(String stageName, int count, BigDecimal value, BigDecimal conversionFromPrevious, BigDecimal overallConversion) {
        this.stageName = stageName;
        this.count = count;
        this.value = value;
        this.conversionFromPrevious = conversionFromPrevious;
        this.overallConversion = overallConversion;
    }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getConversionFromPrevious() { return conversionFromPrevious; }
    public void setConversionFromPrevious(BigDecimal conversionFromPrevious) { this.conversionFromPrevious = conversionFromPrevious; }

    public BigDecimal getOverallConversion() { return overallConversion; }
    public void setOverallConversion(BigDecimal overallConversion) { this.overallConversion = overallConversion; }
}
