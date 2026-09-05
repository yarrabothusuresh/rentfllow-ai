package com.rentflow.aisales.tool;

import com.rentflow.ai.dto.BookingAttentionItemDTO;
import com.rentflow.ai.service.BookingAttentionService;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetBookingsNeedingAttentionTool implements AiSalesTool {

    private final BookingAttentionService bookingAttentionService;

    public GetBookingsNeedingAttentionTool(BookingAttentionService bookingAttentionService) {
        this.bookingAttentionService = bookingAttentionService;
    }

    @Override
    public String getName() {
        return "getBookingsNeedingAttention";
    }

    @Override
    public String getDescription() {
        return "Deterministically identifies upcoming bookings that have operational or financial blockers (inventory conflicts, missing warehouse prep, unassigned drivers, unpaid deposits).";
    }

    @Override
    public boolean isCustomerVisible() {
        return false;
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "OPERATIONS", "WAREHOUSE");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        int daysAhead = 1;
        if (request.getParameters() != null && request.getParameters().containsKey("daysAhead")) {
            try {
                daysAhead = Integer.parseInt(request.getParameters().get("daysAhead").toString());
            } catch (Exception ignored) {}
        }

        try {
            List<BookingAttentionItemDTO> items = bookingAttentionService.getBookingsNeedingAttention(tenantId, daysAhead);
            List<Map<String, Object>> mapped = items.stream().map(i -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("bookingId", i.getBookingId().toString());
                map.put("bookingNumber", i.getBookingNumber());
                map.put("customerName", i.getCustomerName());
                map.put("rentalStart", i.getRentalStart() != null ? i.getRentalStart().toString() : "N/A");
                map.put("severity", i.getSeverity());
                map.put("signals", i.getSignals().stream().map(Enum::name).collect(Collectors.toList()));
                map.put("signalDetails", i.getSignalDetails());
                return map;
            }).collect(Collectors.toList());

            long highCount = items.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();

            return ToolCallResultDTO.success(getName(), Map.of(
                    "daysAhead", daysAhead,
                    "totalBookingsNeedingAttention", items.size(),
                    "highSeverityCount", highCount,
                    "attentionItems", mapped
            ), true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch bookings needing attention: " + e.getMessage(), true);
        }
    }
}
