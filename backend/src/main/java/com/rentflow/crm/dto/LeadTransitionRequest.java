package com.rentflow.crm.dto;

import com.rentflow.crm.model.LeadStage;

public class LeadTransitionRequest {
    private LeadStage targetStage;
    private String reason;
    private String notes;

    public LeadTransitionRequest() {}

    public LeadTransitionRequest(LeadStage targetStage) {
        this.targetStage = targetStage;
    }

    public LeadStage getTargetStage() { return targetStage; }
    public void setTargetStage(LeadStage targetStage) { this.targetStage = targetStage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
