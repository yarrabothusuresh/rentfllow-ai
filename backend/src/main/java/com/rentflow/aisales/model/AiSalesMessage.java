package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_sales_messages", indexes = {
    @Index(name = "idx_aism_tenant", columnList = "tenantId"),
    @Index(name = "idx_aism_conversation", columnList = "conversationId"),
    @Index(name = "idx_aism_created", columnList = "createdAt")
})
public class AiSalesMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiSenderType senderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiMessageType messageType = AiMessageType.TEXT;

    @Enumerated(EnumType.STRING)
    private AiRole role;

    @Column(nullable = false)
    private boolean customerVisible = true;

    private String toolName;
    private String toolCallReference;
    private Integer tokenCount;
    private Long latencyMs;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(length = 8000)
    private String structuredData;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

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

    public AiRole getRole() {
        if (role == null && senderType != null) {
            switch (senderType) {
                case CUSTOMER:
                case SALES_USER:
                    return AiRole.USER;
                case AI:
                    return AiRole.ASSISTANT;
                case SYSTEM:
                    return AiRole.SYSTEM;
            }
        }
        return role;
    }
    public void setRole(AiRole role) { this.role = role; }

    public boolean isCustomerVisible() { return customerVisible; }
    public void setCustomerVisible(boolean customerVisible) { this.customerVisible = customerVisible; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getToolCallReference() { return toolCallReference; }
    public void setToolCallReference(String toolCallReference) { this.toolCallReference = toolCallReference; }

    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
