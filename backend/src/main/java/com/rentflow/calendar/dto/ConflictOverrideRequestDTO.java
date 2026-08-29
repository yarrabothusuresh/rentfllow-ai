package com.rentflow.calendar.dto;

public class ConflictOverrideRequestDTO {
    private String reason;

    public ConflictOverrideRequestDTO() {}
    public ConflictOverrideRequestDTO(String reason) { this.reason = reason; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
