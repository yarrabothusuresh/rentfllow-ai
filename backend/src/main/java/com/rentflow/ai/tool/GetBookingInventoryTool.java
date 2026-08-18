package com.rentflow.ai.tool;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.InventoryReservationDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.BookingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class GetBookingInventoryTool implements AITool {

    private final BookingService bookingService;

    public GetBookingInventoryTool(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "getBookingInventory";
    }

    @Override
    public String getDescription() {
        return "Retrieves the reserved inventory breakdown for a specific booking ID.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            String idStr = (String) parameters.get("bookingId");
            if (idStr == null || idStr.isBlank()) {
                return ToolResult.error("bookingId parameter is required");
            }
            UUID id = UUID.fromString(idStr);

            BookingDTO booking = bookingService.getBookingById(tenantId, id, userRole)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + idStr));

            List<InventoryReservationDTO> reservations = booking.getReservations();
            return ToolResult.ok(reservations, "Found " + reservations.size() + " reserved inventory items for booking " + booking.getBookingNumber());
        } catch (Exception e) {
            return ToolResult.error("Failed to retrieve booking inventory: " + e.getMessage());
        }
    }
}
