package com.rentflow.aisales.dto;

import java.util.Map;
import java.util.UUID;

public class CopilotChatRequestDTO {

    private UUID conversationId;
    private String message;
    private String pageContextType; // e.g. "BOOKING", "CUSTOMER", "LEAD", "INVOICE"
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
