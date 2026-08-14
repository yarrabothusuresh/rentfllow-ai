package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetDeliveriesTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public GetDeliveriesTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "getDeliveries";
    }

    @Override
    public String getDescription() {
        return "Get today's scheduled deliveries, drivers, vehicles, and delivery statuses";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        List<Map<String, Object>> deliveries = demoDataRepository.getDeliveries(request.getTenantId());
        return ToolResult.ok(deliveries, "Retrieved " + deliveries.size() + " delivery job(s)");
    }
}
