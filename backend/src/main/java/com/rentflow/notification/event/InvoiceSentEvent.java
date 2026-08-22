package com.rentflow.notification.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class InvoiceSentEvent {
    private final String tenantId;
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final BigDecimal totalAmount;
    private final BigDecimal balanceDue;
    private final LocalDate dueDate;

    public InvoiceSentEvent(String tenantId, UUID invoiceId, String invoiceNumber, UUID customerId, String customerName, String customerEmail, BigDecimal totalAmount, BigDecimal balanceDue, LocalDate dueDate) {
        this.tenantId = tenantId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.balanceDue = balanceDue;
        this.dueDate = dueDate;
    }

    public String getTenantId() { return tenantId; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getBalanceDue() { return balanceDue; }
    public LocalDate getDueDate() { return dueDate; }
}
