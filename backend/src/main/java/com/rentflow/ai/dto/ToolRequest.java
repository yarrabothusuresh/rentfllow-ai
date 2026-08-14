package com.rentflow.ai.dto;

import java.util.HashMap;
import java.util.Map;

public class ToolRequest {
    private String toolName;
    private Map<String, Object> params = new HashMap<>();
    private String tenantId;
    private String userId;
    private String userRole;

    public ToolRequest() {}

    public ToolRequest(String toolName, Map<String, Object> params, String tenantId, String userId, String userRole) {
        this.toolName = toolName;
        this.params = params != null ? params : new HashMap<>();
        this.tenantId = tenantId;
        this.userId = userId;
        this.userRole = userRole;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
