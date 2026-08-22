package com.rentflow.notification.event;

import java.math.BigDecimal;
import java.util.UUID;

public class QuoteAcceptedEvent {
    private final String tenantId;
    private final UUID quoteId;
    private final String quoteNumber;
    private final UUID customerId;
    private final String customerName;
    private final BigDecimal totalAmount;

    public QuoteAcceptedEvent(String tenantId, UUID quoteId, String quoteNumber, UUID customerId, String customerName, BigDecimal totalAmount) {
        this.tenantId = tenantId;
        this.quoteId = quoteId;
        this.quoteNumber = quoteNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
    }

    public String getTenantId() { return tenantId; }
    public UUID getQuoteId() { return quoteId; }
    public String getQuoteNumber() { return quoteNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
