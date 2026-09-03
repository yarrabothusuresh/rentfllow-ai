package com.rentflow.crm.dto;

import com.rentflow.crm.model.LeadLostReason;

public class LeadLostRequest {
    private LeadLostReason lostReason;
    private String lostReasonNotes;

    public LeadLostRequest() {}

    public LeadLostRequest(LeadLostReason lostReason, String lostReasonNotes) {
        this.lostReason = lostReason;
        this.lostReasonNotes = lostReasonNotes;
    }

    public LeadLostReason getLostReason() { return lostReason; }
    public void setLostReason(LeadLostReason lostReason) { this.lostReason = lostReason; }

    public String getLostReasonNotes() { return lostReasonNotes; }
    public void setLostReasonNotes(String lostReasonNotes) { this.lostReasonNotes = lostReasonNotes; }
}
