package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.AiMessageType;
import com.rentflow.aisales.model.AiSenderType;

import java.time.LocalDateTime;
import java.util.UUID;

public class AiSalesMessageDTO {
    private UUID id;
    private UUID conversationId;
    private AiSenderType senderType;
    private AiMessageType messageType;
    private String content;
    private String structuredData;
    private LocalDateTime createdAt;

    public AiSalesMessageDTO() {}

    public AiSalesMessageDTO(UUID id, UUID conversationId, AiSenderType senderType, AiMessageType messageType, String content, String structuredData, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderType = senderType;
        this.messageType = messageType;
        this.content = content;
        this.structuredData = structuredData;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public AiSenderType getSenderType() { return senderType; }
    public void setSenderType(AiSenderType senderType) { this.senderType = senderType; }

    public AiMessageType getMessageType() { return messageType; }
    public void setMessageType(AiMessageType messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStructuredData() { return structuredData; }
    public void setStructuredData(String structuredData) { this.structuredData = structuredData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
