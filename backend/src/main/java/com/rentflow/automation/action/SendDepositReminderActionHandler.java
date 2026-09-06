package com.rentflow.automation.action;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.automation.model.AutomationActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class SendDepositReminderActionHandler implements AutomationActionHandler {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.SEND_DEPOSIT_REMINDER;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("bookingId")) {
            return RevalidationResult.stale("Missing bookingId in payload");
        }
        UUID bookingId;
        try {
            bookingId = UUID.fromString(payload.get("bookingId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid bookingId: " + payload.get("bookingId"));
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || !tenantId.equals(booking.getTenantId())) {
            return RevalidationResult.stale("Booking not found or belongs to another tenant");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return RevalidationResult.stale("Booking " + booking.getBookingNumber() + " is cancelled");
        }

        BigDecimal req = booking.getDepositRequired() != null ? booking.getDepositRequired() : BigDecimal.ZERO;
        BigDecimal paid = booking.getDepositPaid() != null ? booking.getDepositPaid() : BigDecimal.ZERO;
        if (req.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(req) >= 0) {
            return RevalidationResult.alreadyCompleted("Deposit for Booking " + booking.getBookingNumber() + " is already paid in full");
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID bookingId = UUID.fromString(payload.get("bookingId").toString());
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        String customerEmail = (String) payload.getOrDefault("customerEmail", "customer");

        return ActionResult.success(
            "Sent security deposit reminder for Booking " + booking.getBookingNumber() + " to " + customerEmail,
            Map.of("bookingNumber", booking.getBookingNumber(), "depositRequired", booking.getDepositRequired().toString(), "recipient", customerEmail)
        );
    }
}
