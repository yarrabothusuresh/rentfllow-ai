package com.rentflow.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BookingFinancialSummaryDTO {

    private UUID bookingId;
    private BigDecimal bookingTotal;
    private BigDecimal depositRequired;
    private BigDecimal amountPaid;
    private BigDecimal outstandingBalance;
    private String paymentStatus;

    public BookingFinancialSummaryDTO() {}

    public BookingFinancialSummaryDTO(UUID bookingId, BigDecimal bookingTotal, BigDecimal depositRequired, BigDecimal amountPaid, BigDecimal outstandingBalance, String paymentStatus) {
        this.bookingId = bookingId;
        this.bookingTotal = bookingTotal;
        this.depositRequired = depositRequired;
        this.amountPaid = amountPaid;
        this.outstandingBalance = outstandingBalance;
        this.paymentStatus = paymentStatus;
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public BigDecimal getBookingTotal() { return bookingTotal; }
    public void setBookingTotal(BigDecimal bookingTotal) { this.bookingTotal = bookingTotal; }

    public BigDecimal getDepositRequired() { return depositRequired; }
    public void setDepositRequired(BigDecimal depositRequired) { this.depositRequired = depositRequired; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
