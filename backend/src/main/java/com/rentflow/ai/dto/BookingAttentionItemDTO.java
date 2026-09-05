package com.rentflow.ai.dto;

import com.rentflow.ai.model.BookingAttentionSignal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingAttentionItemDTO {

    private UUID bookingId;
    private String bookingNumber;
    private String customerName;
    private LocalDate eventDate;
    private List<BookingAttentionSignal> signals = new ArrayList<>();
    private List<String> details = new ArrayList<>();
    private String severity; // HIGH, MEDIUM, LOW

    public BookingAttentionItemDTO() {}

    public BookingAttentionItemDTO(UUID bookingId, String bookingNumber, String customerName, LocalDate eventDate, String severity) {
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.customerName = customerName;
        this.eventDate = eventDate;
        this.severity = severity;
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public List<BookingAttentionSignal> getSignals() { return signals; }
    public void setSignals(List<BookingAttentionSignal> signals) { this.signals = signals; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
