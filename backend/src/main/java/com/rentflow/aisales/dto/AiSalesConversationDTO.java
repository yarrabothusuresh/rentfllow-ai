package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.AiConversationStatus;
import com.rentflow.aisales.model.AiIntent;
import com.rentflow.aisales.model.AiSalesChannel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AiSalesConversationDTO {
    private UUID id;
    private String publicId;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private UUID leadId;
    private UUID quoteId;
    private String quoteNumber;
    private AiSalesChannel channel;
    private AiConversationStatus status;
    private AiIntent detectedIntent;
    private String assignedSalesUserId;
    private String internalNotes;
    private LocalDateTime startedAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private RentalInquiryDTO inquiry;
    private List<AiSalesMessageDTO> messages = new ArrayList<>();

    public AiSalesConversationDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public AiSalesChannel getChannel() { return channel; }
    public void setChannel(AiSalesChannel channel) { this.channel = channel; }

    public AiConversationStatus getStatus() { return status; }
    public void setStatus(AiConversationStatus status) { this.status = status; }

    public AiIntent getDetectedIntent() { return detectedIntent; }
    public void setDetectedIntent(AiIntent detectedIntent) { this.detectedIntent = detectedIntent; }

    public String getAssignedSalesUserId() { return assignedSalesUserId; }
    public void setAssignedSalesUserId(String assignedSalesUserId) { this.assignedSalesUserId = assignedSalesUserId; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public RentalInquiryDTO getInquiry() { return inquiry; }
    public void setInquiry(RentalInquiryDTO inquiry) { this.inquiry = inquiry; }

    public List<AiSalesMessageDTO> getMessages() { return messages; }
    public void setMessages(List<AiSalesMessageDTO> messages) { this.messages = messages; }
}
