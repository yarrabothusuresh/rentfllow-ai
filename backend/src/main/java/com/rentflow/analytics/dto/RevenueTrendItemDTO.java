package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class RevenueTrendItemDTO {
    private String period; // e.g. "2026-01" or "2026-08-15"
    private BigDecimal bookedRevenue = BigDecimal.ZERO;
    private BigDecimal invoicedRevenue = BigDecimal.ZERO;
    private BigDecimal collectedRevenue = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private int bookingsCount = 0;

    public RevenueTrendItemDTO() {}

    public RevenueTrendItemDTO(String period, BigDecimal bookedRevenue, BigDecimal invoicedRevenue, BigDecimal collectedRevenue, BigDecimal grossProfit, int bookingsCount) {
        this.period = period;
        this.bookedRevenue = bookedRevenue;
        this.invoicedRevenue = invoicedRevenue;
        this.collectedRevenue = collectedRevenue;
        this.grossProfit = grossProfit;
        this.bookingsCount = bookingsCount;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getBookedRevenue() { return bookedRevenue; }
    public void setBookedRevenue(BigDecimal bookedRevenue) { this.bookedRevenue = bookedRevenue; }

    public BigDecimal getInvoicedRevenue() { return invoicedRevenue; }
    public void setInvoicedRevenue(BigDecimal invoicedRevenue) { this.invoicedRevenue = invoicedRevenue; }

    public BigDecimal getCollectedRevenue() { return collectedRevenue; }
    public void setCollectedRevenue(BigDecimal collectedRevenue) { this.collectedRevenue = collectedRevenue; }

    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }

    public int getBookingsCount() { return bookingsCount; }
    public void setBookingsCount(int bookingsCount) { this.bookingsCount = bookingsCount; }
}
