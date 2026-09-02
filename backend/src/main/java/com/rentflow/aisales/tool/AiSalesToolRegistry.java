package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AiSalesToolRegistry {

    private final Map<String, AiSalesTool> tools = new HashMap<>();

    public AiSalesToolRegistry(List<AiSalesTool> toolList) {
        for (AiSalesTool tool : toolList) {
            tools.put(tool.getName(), tool);
        }
    }

    public Optional<AiSalesTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<AiSalesTool> getAllTools() {
        return tools.values();
    }

    public List<AiSalesTool> getToolsForRole(String role) {
        String cleanRole = role != null ? role.toUpperCase() : "CUSTOMER";
        List<AiSalesTool> allowed = new ArrayList<>();
        for (AiSalesTool tool : tools.values()) {
            if (tool.getAllowedRoles().contains(cleanRole)) {
                allowed.add(tool);
            }
        }
        return allowed;
    }

    public ToolCallResultDTO executeTool(String tenantId, String userRole, ToolCallRequestDTO request) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return ToolCallResultDTO.failure(request.getToolName(), "Invalid tenant context", false);
        }

        AiSalesTool tool = tools.get(request.getToolName());
        if (tool == null) {
            return ToolCallResultDTO.failure(request.getToolName(), "Unknown tool: " + request.getToolName(), false);
        }

        String cleanRole = userRole != null ? userRole.toUpperCase() : "CUSTOMER";
        if (!tool.getAllowedRoles().contains(cleanRole)) {
            return ToolCallResultDTO.failure(request.getToolName(), "Permission denied: role " + cleanRole + " cannot execute tool " + tool.getName(), !tool.isCustomerVisible());
        }

        try {
            return tool.execute(tenantId, cleanRole, request);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(request.getToolName(), "Tool execution error: " + e.getMessage(), !tool.isCustomerVisible());
        }
    }
}
