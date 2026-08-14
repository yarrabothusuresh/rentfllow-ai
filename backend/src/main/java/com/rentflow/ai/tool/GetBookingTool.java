package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetBookingTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public GetBookingTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "getBooking";
    }

    @Override
    public String getDescription() {
        return "Get booking details by bookingId";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String bookingId = request.getParams() != null ? (String) request.getParams().get("bookingId") : "booking-001";
        if (bookingId == null) bookingId = "booking-001";

        List<Map<String, Object>> bookings = demoDataRepository.getBookings(request.getTenantId());
        String finalBookingId = bookingId;
        Optional<Map<String, Object>> match = bookings.stream()
            .filter(b -> finalBookingId.equals(b.get("bookingId")))
            .findFirst();

        if (match.isPresent()) {
            return ToolResult.ok(match.get(), "Booking retrieved successfully");
        }

        if (!bookings.isEmpty()) {
            return ToolResult.ok(bookings.get(0), "Default demo booking retrieved");
        }

        return ToolResult.error("Booking not found");
    }
}
