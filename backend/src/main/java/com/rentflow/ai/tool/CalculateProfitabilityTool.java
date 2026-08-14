package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CalculateProfitabilityTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public CalculateProfitabilityTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "calculateProfitability";
    }

    @Override
    public String getDescription() {
        return "Calculate estimated revenue, cost, profit, and margin for a booking or event";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
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

        double revenue = 6480.00;
        double cost = 2920.00;
        double profit = 3560.00;
        double margin = 54.9;
        String eventName = "Emily Brown Wedding";

        if (match.isPresent()) {
            Map<String, Object> b = match.get();
            revenue = b.get("totalPrice") instanceof Number ? ((Number) b.get("totalPrice")).doubleValue() : revenue;
            cost = b.get("estimatedCost") instanceof Number ? ((Number) b.get("estimatedCost")).doubleValue() : cost;
            profit = b.get("estimatedProfit") instanceof Number ? ((Number) b.get("estimatedProfit")).doubleValue() : profit;
            margin = b.get("estimatedMargin") instanceof Number ? ((Number) b.get("estimatedMargin")).doubleValue() : margin;
            eventName = (String) b.getOrDefault("customerName", eventName);
        }

        Map<String, Object> resultData = Map.of(
            "bookingId", bookingId,
            "eventName", eventName,
            "revenue", revenue,
            "estimatedCost", cost,
            "estimatedProfit", profit,
            "estimatedMargin", margin,
            "isDemoData", true
        );

        return ToolResult.ok(resultData, "Profitability calculated (Demo Data)");
    }
}
