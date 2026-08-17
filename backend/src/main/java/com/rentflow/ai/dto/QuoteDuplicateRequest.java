package com.rentflow.ai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuoteDuplicateRequest {
    private LocalDate newQuoteDate;
    private LocalDate newValidUntil;
    private LocalDateTime newRentalStartDateTime;
    private LocalDateTime newRentalEndDateTime;

    public QuoteDuplicateRequest() {}

    public LocalDate getNewQuoteDate() { return newQuoteDate; }
    public void setNewQuoteDate(LocalDate newQuoteDate) { this.newQuoteDate = newQuoteDate; }

    public LocalDate getNewValidUntil() { return newValidUntil; }
    public void setNewValidUntil(LocalDate newValidUntil) { this.newValidUntil = newValidUntil; }

    public LocalDateTime getNewRentalStartDateTime() { return newRentalStartDateTime; }
    public void setNewRentalStartDateTime(LocalDateTime newRentalStartDateTime) { this.newRentalStartDateTime = newRentalStartDateTime; }

    public LocalDateTime getNewRentalEndDateTime() { return newRentalEndDateTime; }
    public void setNewRentalEndDateTime(LocalDateTime newRentalEndDateTime) { this.newRentalEndDateTime = newRentalEndDateTime; }
}
