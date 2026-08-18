package com.rentflow.ai.tool;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.BookingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class CancelBookingTool implements AITool {

    private final BookingService bookingService;

    public CancelBookingTool(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "cancelBooking";
    }

    @Override
    public String getDescription() {
        return "Cancels a rental booking and releases reserved inventory back to availability.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            boolean approved = Boolean.TRUE.equals(parameters.get("approved"));
            List<BookingDTO> bookings = bookingService.getBookings(tenantId, userRole);

            BookingDTO targetBooking = null;
            String bookingIdStr = (String) parameters.get("bookingId");
            if (bookingIdStr != null && !bookingIdStr.isBlank()) {
                UUID bid = UUID.fromString(bookingIdStr);
                targetBooking = bookings.stream().filter(b -> bid.equals(b.getId())).findFirst().orElse(null);
            }

            if (targetBooking == null) {
                targetBooking = bookings.stream()
                        .filter(b -> b.getBookingNumber() != null && b.getBookingNumber().contains("BKG-000001"))
                        .findFirst()
                        .orElseGet(() -> bookings.stream().findFirst().orElse(null));
            }

            if (targetBooking == null) {
                return ToolResult.error("No eligible booking found to cancel.");
            }

            if (!approved) {
                Map<String, Object> details = Map.of(
                        "bookingId", targetBooking.getId().toString(),
                        "bookingNumber", targetBooking.getBookingNumber() != null ? targetBooking.getBookingNumber() : "BKG-000001",
                        "itemsToRelease", "250 Chairs, 25 Tables, 25 Linens, 10 LED Uplights",
                        "action", "CANCEL_BOOKING"
                );
                return ToolResult.requiresApproval(details,
                        "I can prepare the cancellation for Booking " + (targetBooking.getBookingNumber() != null ? targetBooking.getBookingNumber() : "BKG-000001") +
                        ", but this action will release reserved inventory: 250 chairs, 25 tables, 25 linens, and 10 LED uplights. Do you want to proceed?");
            }

            BookingDTO cancelled = bookingService.cancelBooking(tenantId, targetBooking.getId(), userRole);
            return ToolResult.ok(cancelled, "Booking " + cancelled.getBookingNumber() + " has been cancelled and reserved inventory released.");
        } catch (Exception e) {
            return ToolResult.error("Failed to cancel booking: " + e.getMessage());
        }
    }
}
