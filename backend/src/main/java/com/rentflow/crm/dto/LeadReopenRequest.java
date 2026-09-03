package com.rentflow.crm.dto;

public class LeadReopenRequest {
    private String reopenReason;

    public LeadReopenRequest() {}

    public LeadReopenRequest(String reopenReason) {
        this.reopenReason = reopenReason;
    }

    public String getReopenReason() { return reopenReason; }
    public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }
}
