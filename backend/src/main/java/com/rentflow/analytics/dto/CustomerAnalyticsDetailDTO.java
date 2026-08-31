package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerAnalyticsDetailDTO {
    private UUID customerId;
    private String customerNumber;
    private String customerName;
    private String companyName;
    private String email;
    private String phone;
    private String customerType;

    // Lifetime KPIs
    private BigDecimal lifetimeRevenue = BigDecimal.ZERO;
    private BigDecimal lifetimeCollected = BigDecimal.ZERO;
    private BigDecimal outstandingBalance = BigDecimal.ZERO;
    private BigDecimal lifetimeProfit = BigDecimal.ZERO;
    private BigDecimal lifetimeMarginPercent = BigDecimal.ZERO;
    private int totalBookingsCount = 0;
    private BigDecimal averageBookingValue = BigDecimal.ZERO;
    private BigDecimal quoteConversionRate = BigDecimal.ZERO;
    private int totalQuotesCount = 0;
    private int approvedQuotesCount = 0;
    private int damageClaimsCount = 0;
    private BigDecimal totalDamageCost = BigDecimal.ZERO;
    private LocalDate lastBookingDate;

    // Top rented products & Monthly trend
    private List<NamedMetricDTO> topRentedProducts = new ArrayList<>();
    private List<RevenueTrendItemDTO> revenueTrend = new ArrayList<>();
    private List<BookingProfitabilityDTO> recentBookings = new ArrayList<>();

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

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

    public int getTotalBookingsCount() { return totalBookingsCount; }
    public void setTotalBookingsCount(int totalBookingsCount) { this.totalBookingsCount = totalBookingsCount; }

    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public void setAverageBookingValue(BigDecimal averageBookingValue) { this.averageBookingValue = averageBookingValue; }

    public BigDecimal getQuoteConversionRate() { return quoteConversionRate; }
    public void setQuoteConversionRate(BigDecimal quoteConversionRate) { this.quoteConversionRate = quoteConversionRate; }

    public int getTotalQuotesCount() { return totalQuotesCount; }
    public void setTotalQuotesCount(int totalQuotesCount) { this.totalQuotesCount = totalQuotesCount; }

    public int getApprovedQuotesCount() { return approvedQuotesCount; }
    public void setApprovedQuotesCount(int approvedQuotesCount) { this.approvedQuotesCount = approvedQuotesCount; }

    public int getDamageClaimsCount() { return damageClaimsCount; }
    public void setDamageClaimsCount(int damageClaimsCount) { this.damageClaimsCount = damageClaimsCount; }

    public BigDecimal getTotalDamageCost() { return totalDamageCost; }
    public void setTotalDamageCost(BigDecimal totalDamageCost) { this.totalDamageCost = totalDamageCost; }

    public LocalDate getLastBookingDate() { return lastBookingDate; }
    public void setLastBookingDate(LocalDate lastBookingDate) { this.lastBookingDate = lastBookingDate; }

    public List<NamedMetricDTO> getTopRentedProducts() { return topRentedProducts; }
    public void setTopRentedProducts(List<NamedMetricDTO> topRentedProducts) { this.topRentedProducts = topRentedProducts; }

    public List<RevenueTrendItemDTO> getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(List<RevenueTrendItemDTO> revenueTrend) { this.revenueTrend = revenueTrend; }

    public List<BookingProfitabilityDTO> getRecentBookings() { return recentBookings; }
    public void setRecentBookings(List<BookingProfitabilityDTO> recentBookings) { this.recentBookings = recentBookings; }
}
