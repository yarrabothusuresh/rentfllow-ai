package com.rentflow.notification.dto;

import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationTemplateDTO {
    private UUID id;
    private String tenantId;
    private String name;
    private NotificationType notificationType;
    private NotificationChannel channel;
    private String subject;
    private String body;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationTemplateDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
