package com.rentflow.notification.event;

import java.util.UUID;

public class BookingCancelledEvent {
    private final String tenantId;
    private final UUID bookingId;
    private final String bookingNumber;
    private final UUID customerId;
    private final String customerName;
    private final String reason;

    public BookingCancelledEvent(String tenantId, UUID bookingId, String bookingNumber, UUID customerId, String customerName, String reason) {
        this.tenantId = tenantId;
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.reason = reason;
    }

    public String getTenantId() { return tenantId; }
    public UUID getBookingId() { return bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getReason() { return reason; }
}
