package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.AiConversationStatus;
import com.rentflow.aisales.model.AiIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AiSalesChatResponseDTO {
    private UUID conversationId;
    private String publicId;
    private String replyText;
    private AiConversationStatus status;
    private AiIntent detectedIntent;
    private RentalInquiryDTO inquiry;
    private List<ToolCallResultDTO> executedTools = new ArrayList<>();
    private List<String> suggestedReplies = new ArrayList<>();
    private UUID quoteDraftId;
    private String quoteDraftNumber;
    private boolean escalatedToHuman;
    private String escalationReason;

    public AiSalesChatResponseDTO() {}

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }

    public AiConversationStatus getStatus() { return status; }
    public void setStatus(AiConversationStatus status) { this.status = status; }

    public AiIntent getDetectedIntent() { return detectedIntent; }
    public void setDetectedIntent(AiIntent detectedIntent) { this.detectedIntent = detectedIntent; }

    public RentalInquiryDTO getInquiry() { return inquiry; }
    public void setInquiry(RentalInquiryDTO inquiry) { this.inquiry = inquiry; }

    public List<ToolCallResultDTO> getExecutedTools() { return executedTools; }
    public void setExecutedTools(List<ToolCallResultDTO> executedTools) { this.executedTools = executedTools; }

    public List<String> getSuggestedReplies() { return suggestedReplies; }
    public void setSuggestedReplies(List<String> suggestedReplies) { this.suggestedReplies = suggestedReplies; }

    public UUID getQuoteDraftId() { return quoteDraftId; }
    public void setQuoteDraftId(UUID quoteDraftId) { this.quoteDraftId = quoteDraftId; }

    public String getQuoteDraftNumber() { return quoteDraftNumber; }
    public void setQuoteDraftNumber(String quoteDraftNumber) { this.quoteDraftNumber = quoteDraftNumber; }

    public boolean isEscalatedToHuman() { return escalatedToHuman; }
    public void setEscalatedToHuman(boolean escalatedToHuman) { this.escalatedToHuman = escalatedToHuman; }

    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
}
