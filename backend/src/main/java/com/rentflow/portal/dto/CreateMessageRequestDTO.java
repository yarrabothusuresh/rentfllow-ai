package com.rentflow.portal.dto;

import java.util.UUID;

public class CreateMessageRequestDTO {
    private UUID conversationId;
    private UUID bookingId;
    private UUID quoteId;
    private String subject;
    private String message;

    public CreateMessageRequestDTO() {}

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
