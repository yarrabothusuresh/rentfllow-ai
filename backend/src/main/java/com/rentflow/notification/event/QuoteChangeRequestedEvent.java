package com.rentflow.notification.event;

import java.util.UUID;

public class QuoteChangeRequestedEvent {
    private final String tenantId;
    private final UUID quoteId;
    private final String quoteNumber;
    private final UUID customerId;
    private final String customerName;
    private final String notes;

    public QuoteChangeRequestedEvent(String tenantId, UUID quoteId, String quoteNumber, UUID customerId, String customerName, String notes) {
        this.tenantId = tenantId;
        this.quoteId = quoteId;
        this.quoteNumber = quoteNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.notes = notes;
    }

    public String getTenantId() { return tenantId; }
    public UUID getQuoteId() { return quoteId; }
    public String getQuoteNumber() { return quoteNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getNotes() { return notes; }
}
