package com.rentflow.notification.dto;

import com.rentflow.notification.model.NotificationType;

import java.util.UUID;

public class NotificationPreferenceDTO {
    private UUID id;
    private String tenantId;
    private UUID customerId;
    private UUID userId;
    private NotificationType notificationType;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;

    public NotificationPreferenceDTO() {}

    public NotificationPreferenceDTO(UUID id, String tenantId, UUID customerId, UUID userId, NotificationType notificationType, boolean emailEnabled, boolean smsEnabled, boolean inAppEnabled) {
        this.id = id;
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.inAppEnabled = inAppEnabled;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public boolean isSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }

    public boolean isInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
}
