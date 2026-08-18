package com.rentflow.ai.tool;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.service.BookingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetUpcomingBookingsTool implements AITool {

    private final BookingService bookingService;

    public GetUpcomingBookingsTool(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "getUpcomingBookings";
    }

    @Override
    public String getDescription() {
        return "Retrieves all upcoming confirmed bookings and reservations.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();

        try {
            List<BookingDTO> upcoming = bookingService.getBookings(tenantId, userRole).stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING)
                    .collect(Collectors.toList());

            return ToolResult.ok(upcoming, "Found " + upcoming.size() + " upcoming bookings.");
        } catch (Exception e) {
            return ToolResult.error("Failed to retrieve upcoming bookings: " + e.getMessage());
        }
    }
}
