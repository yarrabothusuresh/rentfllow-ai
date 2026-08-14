package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CheckAvailabilityTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public CheckAvailabilityTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "checkAvailability";
    }

    @Override
    public String getDescription() {
        return "Check inventory item availability for a given date and quantity";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        Map<String, Object> params = request.getParams() != null ? request.getParams() : new HashMap<>();
        String productId = (String) params.getOrDefault("productId", "chair-001");
        int requestedQuantity = params.get("quantity") instanceof Number ? ((Number) params.get("quantity")).intValue() : 250;
        String eventDate = (String) params.getOrDefault("eventDate", "2026-09-20");

        List<Map<String, Object>> products = demoDataRepository.getProducts(request.getTenantId());
        Optional<Map<String, Object>> prodOpt = products.stream()
            .filter(p -> productId.equals(p.get("productId")) || "Chiavari Chairs".equals(p.get("name")))
            .findFirst();

        int availableQty = 300;
        if (prodOpt.isPresent()) {
            availableQty = (int) prodOpt.get().get("availableQuantity");
        }

        boolean isAvailable = availableQty >= requestedQuantity;

        Map<String, Object> resultData = Map.of(
            "productId", productId,
            "productName", prodOpt.isPresent() ? prodOpt.get().get("name") : "Chiavari Chairs",
            "available", isAvailable,
            "requested", requestedQuantity,
            "availableQuantity", availableQty,
            "eventDate", eventDate
        );

        return ToolResult.ok(resultData, isAvailable ? "Sufficient inventory available" : "Insufficient inventory");
    }
}
