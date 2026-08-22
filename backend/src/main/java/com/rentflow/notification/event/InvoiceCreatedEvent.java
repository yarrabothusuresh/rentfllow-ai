package com.rentflow.notification.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class InvoiceCreatedEvent {
    private final String tenantId;
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final BigDecimal totalAmount;
    private final LocalDate dueDate;

    public InvoiceCreatedEvent(String tenantId, UUID invoiceId, String invoiceNumber, UUID customerId, String customerName, BigDecimal totalAmount, LocalDate dueDate) {
        this.tenantId = tenantId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

    public String getTenantId() { return tenantId; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDate getDueDate() { return dueDate; }
}
