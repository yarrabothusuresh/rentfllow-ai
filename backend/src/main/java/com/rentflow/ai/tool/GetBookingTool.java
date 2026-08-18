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
import java.util.stream.Collectors;

@Component
public class GetBookingTool implements AITool {

    private final BookingService bookingService;

    public GetBookingTool(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "getBooking";
    }

    @Override
    public String getDescription() {
        return "Retrieves a booking by ID, booking number (e.g. BKG-000001), customer, or event.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            List<BookingDTO> bookings = bookingService.getBookings(tenantId, userRole);

            String idStr = (String) parameters.get("bookingId");
            String numberStr = (String) parameters.get("bookingNumber");
            String query = (String) parameters.get("query");

            if (idStr != null && !idStr.isBlank()) {
                UUID id = UUID.fromString(idStr);
                return bookingService.getBookingById(tenantId, id, userRole)
                        .map(b -> ToolResult.ok(b, "Found booking " + b.getBookingNumber()))
                        .orElse(ToolResult.error("Booking not found with ID: " + idStr));
            }

            if (numberStr != null && !numberStr.isBlank()) {
                List<BookingDTO> matched = bookings.stream()
                        .filter(b -> numberStr.equalsIgnoreCase(b.getBookingNumber()))
                        .collect(Collectors.toList());
                if (!matched.isEmpty()) {
                    return ToolResult.ok(matched.get(0), "Found booking " + matched.get(0).getBookingNumber());
                }
            }

            if (query != null && !query.isBlank()) {
                String qLower = query.toLowerCase();
                List<BookingDTO> matched = bookings.stream()
                        .filter(b -> (b.getBookingNumber() != null && b.getBookingNumber().toLowerCase().contains(qLower)) ||
                                     (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(qLower)) ||
                                     (b.getEventName() != null && b.getEventName().toLowerCase().contains(qLower)))
                        .collect(Collectors.toList());
                if (!matched.isEmpty()) {
                    return ToolResult.ok(matched.get(0), "Found booking matching query: " + query);
                }
            }

            if (!bookings.isEmpty()) {
                return ToolResult.ok(bookings.get(0), "Retrieved booking " + bookings.get(0).getBookingNumber());
            }

            return ToolResult.error("No bookings found");
        } catch (Exception e) {
            return ToolResult.error("Failed to retrieve booking: " + e.getMessage());
        }
    }
}
