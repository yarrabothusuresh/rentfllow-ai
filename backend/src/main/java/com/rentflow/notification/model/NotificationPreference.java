package com.rentflow.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notifpref_tenant", columnList = "tenantId"),
    @Index(name = "idx_notifpref_cust", columnList = "tenantId, customerId"),
    @Index(name = "idx_notifpref_user", columnList = "tenantId, userId")
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    private UUID customerId;
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private boolean emailEnabled = true;

    @Column(nullable = false)
    private boolean smsEnabled = true;

    @Column(nullable = false)
    private boolean inAppEnabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NotificationPreference() {}

    public NotificationPreference(String tenantId, UUID customerId, UUID userId, NotificationType notificationType, boolean emailEnabled, boolean smsEnabled, boolean inAppEnabled) {
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
