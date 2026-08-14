package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SearchCustomerTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public SearchCustomerTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "searchCustomer";
    }

    @Override
    public String getDescription() {
        return "Search customer records by name or query string";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String query = request.getParams() != null ? (String) request.getParams().get("query") : "";
        if (query == null) query = "";

        List<Map<String, Object>> customers = demoDataRepository.getCustomers(request.getTenantId());
        String qLower = query.toLowerCase();

        List<Map<String, Object>> matches = customers.stream()
            .filter(c -> {
                String name = (String) c.get("name");
                return name != null && name.toLowerCase().contains(qLower);
            })
            .toList();

        if (!matches.isEmpty()) {
            Map<String, Object> match = matches.get(0);
            return ToolResult.ok(match, "Found customer matching query: " + query);
        } else if (!customers.isEmpty()) {
            return ToolResult.ok(customers.get(0), "No direct match for '" + query + "', returning top customer");
        }

        return ToolResult.error("No customers found");
    }
}
