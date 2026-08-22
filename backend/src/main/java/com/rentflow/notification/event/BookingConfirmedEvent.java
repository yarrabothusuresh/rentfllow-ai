package com.rentflow.notification.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookingConfirmedEvent {
    private final String tenantId;
    private final UUID bookingId;
    private final String bookingNumber;
    private final UUID customerId;
    private final String customerName;
    private final String eventName;
    private final LocalDateTime eventDate;

    public BookingConfirmedEvent(String tenantId, UUID bookingId, String bookingNumber, UUID customerId, String customerName, String eventName, LocalDateTime eventDate) {
        this.tenantId = tenantId;
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }

    public String getTenantId() { return tenantId; }
    public UUID getBookingId() { return bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getEventName() { return eventName; }
    public LocalDateTime getEventDate() { return eventDate; }
}
