package com.rentflow.aisales.dto;

public class ToolCallResultDTO {
    private String toolName;
    private boolean success;
    private Object result;
    private String errorMessage;
    private boolean internalOnly;

    public ToolCallResultDTO() {}

    public ToolCallResultDTO(String toolName, boolean success, Object result, String errorMessage, boolean internalOnly) {
        this.toolName = toolName;
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
        this.internalOnly = internalOnly;
    }

    public static ToolCallResultDTO success(String toolName, Object result, boolean internalOnly) {
        return new ToolCallResultDTO(toolName, true, result, null, internalOnly);
    }

    public static ToolCallResultDTO failure(String toolName, String errorMessage, boolean internalOnly) {
        return new ToolCallResultDTO(toolName, false, null, errorMessage, internalOnly);
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }

    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> getData() {
        if (result instanceof java.util.Map) {
            return (java.util.Map<String, Object>) result;
        }
        return java.util.Collections.emptyMap();
    }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean isInternalOnly() { return internalOnly; }
    public void setInternalOnly(boolean internalOnly) { this.internalOnly = internalOnly; }
}
