package com.rentflow.warehouse.dto;

import java.util.List;
import java.util.UUID;

public class PickVerificationRequestDTO {
    private List<UUID> verifiedItemIds;
    private List<UUID> acknowledgedExceptionIds;
    private String notes;

    public List<UUID> getVerifiedItemIds() { return verifiedItemIds; }
    public void setVerifiedItemIds(List<UUID> verifiedItemIds) { this.verifiedItemIds = verifiedItemIds; }

    public List<UUID> getAcknowledgedExceptionIds() { return acknowledgedExceptionIds; }
    public void setAcknowledgedExceptionIds(List<UUID> acknowledgedExceptionIds) { this.acknowledgedExceptionIds = acknowledgedExceptionIds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
