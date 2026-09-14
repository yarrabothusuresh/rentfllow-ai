package com.rentflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AIRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must not exceed 4000 characters")
    private String message;

    @Size(max = 255, message = "UserId must not exceed 255 characters")
    private String userId;

    @Size(max = 255, message = "TenantId must not exceed 255 characters")
    private String tenantId;

    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role;

    @Size(max = 255, message = "ConversationId must not exceed 255 characters")
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
