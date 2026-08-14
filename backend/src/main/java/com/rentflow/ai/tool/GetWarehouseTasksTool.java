package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetWarehouseTasksTool implements AITool {

    private final DemoDataRepository demoDataRepository;

    public GetWarehouseTasksTool(DemoDataRepository demoDataRepository) {
        this.demoDataRepository = demoDataRepository;
    }

    @Override
    public String getName() {
        return "getWarehouseTasks";
    }

    @Override
    public String getDescription() {
        return "Get warehouse operational tasks (Pick Lists, Pack Lists, Load Lists, Returns)";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "WAREHOUSE");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        List<Map<String, Object>> tasks = demoDataRepository.getWarehouseTasks(request.getTenantId());
        return ToolResult.ok(tasks, "Retrieved warehouse operational tasks");
    }
}
