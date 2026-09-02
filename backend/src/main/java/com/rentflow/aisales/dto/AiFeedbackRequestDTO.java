package com.rentflow.aisales.dto;

import java.util.UUID;

public class AiFeedbackRequestDTO {
    private UUID conversationId;
    private UUID messageId;
    private boolean helpful;
    private String reason;
    private String comments;

    public AiFeedbackRequestDTO() {}

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }

    public boolean isHelpful() { return helpful; }
    public void setHelpful(boolean helpful) { this.helpful = helpful; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
