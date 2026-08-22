package com.rentflow.notification.event;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentReceivedEvent {
    private final String tenantId;
    private final UUID paymentId;
    private final UUID bookingId;
    private final String bookingNumber;
    private final UUID customerId;
    private final String customerName;
    private final BigDecimal amount;
    private final BigDecimal balanceDue;

    public PaymentReceivedEvent(String tenantId, UUID paymentId, UUID bookingId, String bookingNumber, UUID customerId, String customerName, BigDecimal amount, BigDecimal balanceDue) {
        this.tenantId = tenantId;
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.amount = amount;
        this.balanceDue = balanceDue;
    }

    public String getTenantId() { return tenantId; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getBookingId() { return bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceDue() { return balanceDue; }
}
