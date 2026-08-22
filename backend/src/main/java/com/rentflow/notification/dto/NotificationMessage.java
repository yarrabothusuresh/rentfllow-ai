package com.rentflow.notification.dto;

import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;

import java.util.UUID;

public class NotificationMessage {
    private String tenantId;
    private UUID recipientUserId;
    private UUID recipientCustomerId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private String referenceType;
    private String referenceId;
    private NotificationPriority priority;

    public NotificationMessage() {}

    public NotificationMessage(String tenantId, UUID recipientUserId, UUID recipientCustomerId, String recipientEmail, String recipientPhone, NotificationType type, NotificationChannel channel, String title, String message, String referenceType, String referenceId, NotificationPriority priority) {
        this.tenantId = tenantId;
        this.recipientUserId = recipientUserId;
        this.recipientCustomerId = recipientCustomerId;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.priority = priority;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }

    public UUID getRecipientCustomerId() { return recipientCustomerId; }
    public void setRecipientCustomerId(UUID recipientCustomerId) { this.recipientCustomerId = recipientCustomerId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

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

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
}
