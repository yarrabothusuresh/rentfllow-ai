package com.rentflow.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_tenant", columnList = "tenantId"),
    @Index(name = "idx_notif_user", columnList = "recipientUserId"),
    @Index(name = "idx_notif_cust", columnList = "recipientCustomerId"),
    @Index(name = "idx_notif_status", columnList = "status"),
    @Index(name = "idx_notif_ref", columnList = "referenceType, referenceId")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    private UUID recipientUserId;
    private UUID recipientCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String message;

    private String referenceType;
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;

    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;

    private int retryCount = 0;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = NotificationStatus.PENDING;
        if (priority == null) priority = NotificationPriority.NORMAL;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Notification() {}

    public Notification(String tenantId, UUID recipientUserId, UUID recipientCustomerId, NotificationType type, NotificationChannel channel, String title, String message, String referenceType, String referenceId, NotificationPriority priority) {
        this.tenantId = tenantId;
        this.recipientUserId = recipientUserId;
        this.recipientCustomerId = recipientCustomerId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.status = NotificationStatus.PENDING;
        this.priority = priority != null ? priority : NotificationPriority.NORMAL;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }

    public UUID getRecipientCustomerId() { return recipientCustomerId; }
    public void setRecipientCustomerId(UUID recipientCustomerId) { this.recipientCustomerId = recipientCustomerId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
