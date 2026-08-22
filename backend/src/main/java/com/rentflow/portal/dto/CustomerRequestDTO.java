package com.rentflow.portal.dto;

import com.rentflow.portal.model.RequestStatus;
import com.rentflow.portal.model.RequestType;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerRequestDTO {
    private UUID id;
    private RequestType requestType;
    private String subject;
    private String message;
    private RequestStatus status;
    private UUID quoteId;
    private UUID bookingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerRequestDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
