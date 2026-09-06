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

    public void setParameters(Map<String, ?> parameters) {
        if (parameters != null) {
            this.arguments = new HashMap<>(parameters);
        }
    }

    public Map<String, Object> getParameters() {
        return arguments;
    }

    public String getStringParam(String key, String defaultValue) {
        if (arguments != null && arguments.containsKey(key) && arguments.get(key) != null) {
            return arguments.get(key).toString();
        }
        return defaultValue;
    }
}
