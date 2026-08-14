package com.rentflow.ai.dto;

import java.util.UUID;

public class AIRequest {
    private String message;
    private String userId;
    private String tenantId;
    private String role;
    private String conversationId;

    public AIRequest() {}

    public AIRequest(String message, String userId, String tenantId, String role, String conversationId) {
        this.message = message;
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
