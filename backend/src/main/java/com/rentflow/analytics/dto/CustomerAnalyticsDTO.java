package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CustomerAnalyticsDTO {
    private UUID customerId;
    private String customerNumber;
    private String customerName;
    private String companyName;
    private String customerType;

    private int totalBookingsCount = 0;
    private BigDecimal lifetimeRevenue = BigDecimal.ZERO;
    private BigDecimal lifetimeCollected = BigDecimal.ZERO;
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    private BigDecimal lifetimeProfit = BigDecimal.ZERO;
    private BigDecimal lifetimeMarginPercent = BigDecimal.ZERO;
    private BigDecimal averageBookingValue = BigDecimal.ZERO;
    private BigDecimal quoteConversionRate = BigDecimal.ZERO;
    private int damageClaimsCount = 0;
    private LocalDate lastBookingDate;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public int getTotalBookingsCount() { return totalBookingsCount; }
    public void setTotalBookingsCount(int totalBookingsCount) { this.totalBookingsCount = totalBookingsCount; }

    public BigDecimal getLifetimeRevenue() { return lifetimeRevenue; }
    public void setLifetimeRevenue(BigDecimal lifetimeRevenue) { this.lifetimeRevenue = lifetimeRevenue; }

    public BigDecimal getLifetimeCollected() { return lifetimeCollected; }
    public void setLifetimeCollected(BigDecimal lifetimeCollected) { this.lifetimeCollected = lifetimeCollected; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public BigDecimal getLifetimeProfit() { return lifetimeProfit; }
    public void setLifetimeProfit(BigDecimal lifetimeProfit) { this.lifetimeProfit = lifetimeProfit; }

    public BigDecimal getLifetimeMarginPercent() { return lifetimeMarginPercent; }
    public void setLifetimeMarginPercent(BigDecimal lifetimeMarginPercent) { this.lifetimeMarginPercent = lifetimeMarginPercent; }

    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public void setAverageBookingValue(BigDecimal averageBookingValue) { this.averageBookingValue = averageBookingValue; }

    public BigDecimal getQuoteConversionRate() { return quoteConversionRate; }
    public void setQuoteConversionRate(BigDecimal quoteConversionRate) { this.quoteConversionRate = quoteConversionRate; }

    public int getDamageClaimsCount() { return damageClaimsCount; }
    public void setDamageClaimsCount(int damageClaimsCount) { this.damageClaimsCount = damageClaimsCount; }

    public LocalDate getLastBookingDate() { return lastBookingDate; }
    public void setLastBookingDate(LocalDate lastBookingDate) { this.lastBookingDate = lastBookingDate; }
}
