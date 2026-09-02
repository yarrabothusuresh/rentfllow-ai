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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
