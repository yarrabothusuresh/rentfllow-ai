package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SearchProductsTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public SearchProductsTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "searchProducts";
    }

    @Override
    public String getDescription() {
        return "Search catalog products by query keyword and quantity";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String query = request.getParams() != null ? (String) request.getParams().get("query") : "chairs";
        if (query == null) query = "chairs";

        List<Map<String, Object>> products = demoDataRepository.getProducts(request.getTenantId());
        String qLower = query.toLowerCase();

        List<Map<String, Object>> matches = products.stream()
            .filter(p -> {
                String name = (String) p.get("name");
                String cat = (String) p.get("category");
                return (name != null && name.toLowerCase().contains(qLower)) ||
                       (cat != null && cat.toLowerCase().contains(qLower));
            })
            .toList();

        if (!matches.isEmpty()) {
            return ToolResult.ok(matches, "Found " + matches.size() + " product(s) matching '" + query + "'");
        }

        return ToolResult.ok(List.of(
            Map.of(
                "productId", "chair-001",
                "name", "Chiavari Chairs",
                "availableQuantity", 300,
                "rentalPrice", 8.00
            )
        ), "Catalog search result");
    }
}
