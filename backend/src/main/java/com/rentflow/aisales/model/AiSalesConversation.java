package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_sales_conversations", indexes = {
    @Index(name = "idx_aisales_tenant", columnList = "tenantId"),
    @Index(name = "idx_aisales_customer", columnList = "customerId"),
    @Index(name = "idx_aisales_status", columnList = "status"),
    @Index(name = "idx_aisales_public_id", columnList = "publicId")
})
public class AiSalesConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String publicId;

    private UUID customerId;
    private UUID leadId;
    private UUID quoteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiSalesChannel channel = AiSalesChannel.INTERNAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiConversationStatus status = AiConversationStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private AiIntent detectedIntent;

    private String assignedSalesUserId;
    private String customerName;
    private String customerEmail;

    @Column(length = 2000)
    private String internalNotes;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime lastMessageAt;
    private LocalDateTime closedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.startedAt == null) this.startedAt = LocalDateTime.now();
        if (this.lastMessageAt == null) this.lastMessageAt = LocalDateTime.now();
        if (this.publicId == null) {
            this.publicId = "conv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public AiSalesChannel getChannel() { return channel; }
    public void setChannel(AiSalesChannel channel) { this.channel = channel; }

    public AiConversationStatus getStatus() { return status; }
    public void setStatus(AiConversationStatus status) { this.status = status; }

    public AiIntent getDetectedIntent() { return detectedIntent; }
    public void setDetectedIntent(AiIntent detectedIntent) { this.detectedIntent = detectedIntent; }

    public String getAssignedSalesUserId() { return assignedSalesUserId; }
    public void setAssignedSalesUserId(String assignedSalesUserId) { this.assignedSalesUserId = assignedSalesUserId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
