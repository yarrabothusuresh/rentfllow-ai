package com.rentflow.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_messages", indexes = {
    @Index(name = "idx_msg_conversation", columnList = "conversation_id")
})
public class CustomerMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private CustomerConversation conversation;

    @Column(nullable = false)
    private String senderType; // CUSTOMER, STAFF

    private String senderId;

    @Column(nullable = false, length = 4000)
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public CustomerMessage() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CustomerConversation getConversation() { return conversation; }
    public void setConversation(CustomerConversation conversation) { this.conversation = conversation; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
