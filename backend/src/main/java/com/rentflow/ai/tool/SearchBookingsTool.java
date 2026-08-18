package com.rentflow.ai.tool;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.BookingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SearchBookingsTool implements AITool {

    private final BookingService bookingService;

    public SearchBookingsTool(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "searchBookings";
    }

    @Override
    public String getDescription() {
        return "Searches bookings by customer name, event name, booking number, or status.";
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
            String query = (String) parameters.get("query");
            List<BookingDTO> bookings = bookingService.getBookings(tenantId, userRole);

            if (query != null && !query.isBlank()) {
                String qLower = query.toLowerCase();
                bookings = bookings.stream()
                        .filter(b -> (b.getBookingNumber() != null && b.getBookingNumber().toLowerCase().contains(qLower)) ||
                                     (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(qLower)) ||
                                     (b.getEventName() != null && b.getEventName().toLowerCase().contains(qLower)) ||
                                     (b.getStatus() != null && b.getStatus().name().toLowerCase().contains(qLower)))
                        .collect(Collectors.toList());
            }

            return ToolResult.ok(bookings, "Found " + bookings.size() + " matching bookings.");
        } catch (Exception e) {
            return ToolResult.error("Failed to search bookings: " + e.getMessage());
        }
    }
}
