package com.rentflow.notification.event;

import java.util.UUID;

public class CustomerRequestCreatedEvent {
    private final String tenantId;
    private final UUID requestId;
    private final UUID customerId;
    private final String customerName;
    private final String requestType;
    private final String subject;
    private final String message;

    public CustomerRequestCreatedEvent(String tenantId, UUID requestId, UUID customerId, String customerName, String requestType, String subject, String message) {
        this.tenantId = tenantId;
        this.requestId = requestId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.requestType = requestType;
        this.subject = subject;
        this.message = message;
    }

    public String getTenantId() { return tenantId; }
    public UUID getRequestId() { return requestId; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getRequestType() { return requestType; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
}
