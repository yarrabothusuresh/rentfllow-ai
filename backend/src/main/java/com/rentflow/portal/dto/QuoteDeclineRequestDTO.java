package com.rentflow.portal.dto;

public class QuoteDeclineRequestDTO {
    private String reason;

    public QuoteDeclineRequestDTO() {}

    public QuoteDeclineRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
