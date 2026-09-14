package com.rentflow.aisales.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public class CopilotChatRequestDTO {

    private UUID conversationId;

    @Size(max = 4000, message = "Message must not exceed 4000 characters")
    private String message;

    @Size(max = 100, message = "PageContextType must not exceed 100 characters")
    private String pageContextType; // e.g. "BOOKING", "CUSTOMER", "LEAD", "INVOICE"

    @Size(max = 255, message = "PageContextId must not exceed 255 characters")
    private String pageContextId;   // e.g. "BOOK-000001" or UUID

    private Map<String, Object> extraContext;

    public CopilotChatRequestDTO() {}

    public CopilotChatRequestDTO(String message) {
        this.message = message;
    }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPageContextType() { return pageContextType; }
    public void setPageContextType(String pageContextType) { this.pageContextType = pageContextType; }

    public String getPageContextId() { return pageContextId; }
    public void setPageContextId(String pageContextId) { this.pageContextId = pageContextId; }

    public Map<String, Object> getExtraContext() { return extraContext; }
    public void setExtraContext(Map<String, Object> extraContext) { this.extraContext = extraContext; }
}
