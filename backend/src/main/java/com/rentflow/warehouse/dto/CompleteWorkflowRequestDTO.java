package com.rentflow.warehouse.dto;

public class CompleteWorkflowRequestDTO {

    private boolean confirmShortage;
    private String notes;

    public boolean isConfirmShortage() { return confirmShortage; }
    public void setConfirmShortage(boolean confirmShortage) { this.confirmShortage = confirmShortage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
