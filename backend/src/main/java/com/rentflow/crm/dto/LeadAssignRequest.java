package com.rentflow.crm.dto;

public class LeadAssignRequest {
    private String assignedSalesUserId;
    private String assignedSalesUserName;

    public LeadAssignRequest() {}

    public LeadAssignRequest(String assignedSalesUserId, String assignedSalesUserName) {
        this.assignedSalesUserId = assignedSalesUserId;
        this.assignedSalesUserName = assignedSalesUserName;
    }

    public String getAssignedSalesUserId() { return assignedSalesUserId; }
    public void setAssignedSalesUserId(String assignedSalesUserId) { this.assignedSalesUserId = assignedSalesUserId; }

    public String getAssignedSalesUserName() { return assignedSalesUserName; }
    public void setAssignedSalesUserName(String assignedSalesUserName) { this.assignedSalesUserName = assignedSalesUserName; }
}
