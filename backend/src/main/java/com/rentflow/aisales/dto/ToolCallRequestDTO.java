package com.rentflow.aisales.dto;

import java.util.HashMap;
import java.util.Map;

public class ToolCallRequestDTO {
    private String toolName;
    private Map<String, Object> arguments = new HashMap<>();

    public ToolCallRequestDTO() {}

    public ToolCallRequestDTO(String toolName, Map<String, Object> arguments) {
        this.toolName = toolName;
        this.arguments = arguments != null ? arguments : new HashMap<>();
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public Map<String, Object> getArguments() { return arguments; }
    public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
}
