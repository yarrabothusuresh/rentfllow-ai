package com.rentflow.portal.dto;

import com.rentflow.portal.model.RequestType;
import java.util.UUID;

public class CreateCustomerRequestDTO {
    private RequestType type;
    private String subject;
    private String message;
    private UUID quoteId;
    private UUID bookingId;

    public CreateCustomerRequestDTO() {}

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
}
