package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetUpcomingBookingsTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public GetUpcomingBookingsTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "getUpcomingBookings";
    }

    @Override
    public String getDescription() {
        return "Retrieve upcoming bookings and event schedule";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        List<Map<String, Object>> bookings = demoDataRepository.getBookings(request.getTenantId());
        return ToolResult.ok(bookings, "Retrieved " + bookings.size() + " upcoming booking(s)");
    }
}
