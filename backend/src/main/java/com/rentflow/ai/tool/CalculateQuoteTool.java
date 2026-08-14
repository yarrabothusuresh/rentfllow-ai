package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CalculateQuoteTool implements AITool {

    @Override
    public String getName() {
        return "calculateQuote";
    }

    @Override
    public String getDescription() {
        return "Calculate estimated quote pricing breakdown (Rental, Delivery, Setup, Total)";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        Map<String, Object> params = request.getParams() != null ? request.getParams() : new HashMap<>();
        String eventType = (String) params.getOrDefault("eventType", "Wedding");
        int guestCount = params.get("guestCount") instanceof Number ? ((Number) params.get("guestCount")).intValue() : 250;

        double rentalAmount = guestCount * 19.40; // 4850 for 250 guests
        double deliveryAmount = 750.00;
        double setupAmount = 400.00;
        double totalAmount = rentalAmount + deliveryAmount + setupAmount;

        Map<String, Object> quoteResult = Map.of(
            "eventType", eventType,
            "guestCount", guestCount,
            "rentalAmount", rentalAmount,
            "deliveryAmount", deliveryAmount,
            "setupAmount", setupAmount,
            "estimatedTotal", totalAmount,
            "currency", "USD",
            "isDemo", true
        );

        return ToolResult.ok(quoteResult, "Quote calculated successfully");
    }
}
