package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class BookingProfitabilityDTO {
    private UUID bookingId;
    private String bookingNumber;
    private String customerName;
    private String eventName;
    private String status;
    private LocalDate bookingDate;

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal directCost = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal grossMarginPercent = BigDecimal.ZERO;

    private String marginFlag; // HIGH_MARGIN, HEALTHY_MARGIN, LOW_MARGIN, NEGATIVE_MARGIN
    private boolean warningFlag;

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getDirectCost() { return directCost; }
    public void setDirectCost(BigDecimal directCost) { this.directCost = directCost; }

    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }

    public BigDecimal getGrossMarginPercent() { return grossMarginPercent; }
    public void setGrossMarginPercent(BigDecimal grossMarginPercent) { this.grossMarginPercent = grossMarginPercent; }

    public String getMarginFlag() { return marginFlag; }
    public void setMarginFlag(String marginFlag) { this.marginFlag = marginFlag; }

    public boolean isWarningFlag() { return warningFlag; }
    public boolean getWarningFlag() { return warningFlag; }
    public void setWarningFlag(boolean warningFlag) { this.warningFlag = warningFlag; }
}
