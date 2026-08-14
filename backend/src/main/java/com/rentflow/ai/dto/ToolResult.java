package com.rentflow.ai.dto;

public class ToolResult {
    private boolean success;
    private Object data;
    private String status; // "OK", "ACTION_REQUIRES_APPROVAL", "PERMISSION_DENIED", "ERROR"
    private String message;

    public ToolResult() {}

    public ToolResult(boolean success, Object data, String status, String message) {
        this.success = success;
        this.data = data;
        this.status = status;
        this.message = message;
    }

    public static ToolResult ok(Object data, String message) {
        return new ToolResult(true, data, "OK", message);
    }

    public static ToolResult requiresApproval(Object data, String message) {
        return new ToolResult(true, data, "ACTION_REQUIRES_APPROVAL", message);
    }

    public static ToolResult permissionDenied(String message) {
        return new ToolResult(false, null, "PERMISSION_DENIED", message);
    }

    public static ToolResult error(String message) {
        return new ToolResult(false, null, "ERROR", message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
