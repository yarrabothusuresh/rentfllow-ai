package com.rentflow.ai.dto;

import java.util.UUID;

public class LeadConversionResult {
    private UUID leadId;
    private UUID customerId;
    private UUID eventId;
    private String customerNumber;
    private String status;
    private String message;
    private boolean possibleDuplicateFound;
    private CustomerDTO duplicateCustomer;

    public LeadConversionResult() {}

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isPossibleDuplicateFound() { return possibleDuplicateFound; }
    public void setPossibleDuplicateFound(boolean possibleDuplicateFound) { this.possibleDuplicateFound = possibleDuplicateFound; }

    public CustomerDTO getDuplicateCustomer() { return duplicateCustomer; }
    public void setDuplicateCustomer(CustomerDTO duplicateCustomer) { this.duplicateCustomer = duplicateCustomer; }
}
