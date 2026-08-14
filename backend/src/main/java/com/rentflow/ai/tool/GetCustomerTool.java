package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetCustomerTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public GetCustomerTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "getCustomer";
    }

    @Override
    public String getDescription() {
        return "Get detailed customer information by customerId";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String customerId = request.getParams() != null ? (String) request.getParams().get("customerId") : "customer-001";
        if (customerId == null) customerId = "customer-001";

        List<Map<String, Object>> customers = demoDataRepository.getCustomers(request.getTenantId());
        String finalCustomerId = customerId;
        Optional<Map<String, Object>> match = customers.stream()
            .filter(c -> finalCustomerId.equals(c.get("customerId")))
            .findFirst();

        if (match.isPresent()) {
            return ToolResult.ok(match.get(), "Customer record retrieved successfully");
        }

        return ToolResult.ok(Map.of(
            "customerId", "customer-001",
            "name", "Emily Brown",
            "city", "Dallas",
            "activeBookings", 1
        ), "Retrieved demo customer");
    }
}
