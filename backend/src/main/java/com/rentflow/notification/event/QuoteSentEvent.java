package com.rentflow.notification.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuoteSentEvent {
    private final String tenantId;
    private final UUID quoteId;
    private final String quoteNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final String eventName;
    private final BigDecimal totalAmount;
    private final LocalDateTime validUntil;

    public QuoteSentEvent(String tenantId, UUID quoteId, String quoteNumber, UUID customerId, String customerName, String customerEmail, String eventName, BigDecimal totalAmount, LocalDateTime validUntil) {
        this.tenantId = tenantId;
        this.quoteId = quoteId;
        this.quoteNumber = quoteNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.eventName = eventName;
        this.totalAmount = totalAmount;
        this.validUntil = validUntil;
    }

    public String getTenantId() { return tenantId; }
    public UUID getQuoteId() { return quoteId; }
    public String getQuoteNumber() { return quoteNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getEventName() { return eventName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getValidUntil() { return validUntil; }
}
