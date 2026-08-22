package com.rentflow.notification.dto;

import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;

import java.util.Map;
import java.util.UUID;

public class NotificationRequestDTO {
    private String tenantId;
    private UUID recipientUserId;
    private UUID recipientCustomerId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private String referenceType;
    private String referenceId;
    private Map<String, Object> templateVariables;
    private String customTitle;
    private String customMessage;

    public NotificationRequestDTO() {}

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

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public Map<String, Object> getTemplateVariables() { return templateVariables; }
    public void setTemplateVariables(Map<String, Object> templateVariables) { this.templateVariables = templateVariables; }

    public String getCustomTitle() { return customTitle; }
    public void setCustomTitle(String customTitle) { this.customTitle = customTitle; }

    public String getCustomMessage() { return customMessage; }
    public void setCustomMessage(String customMessage) { this.customMessage = customMessage; }
}
