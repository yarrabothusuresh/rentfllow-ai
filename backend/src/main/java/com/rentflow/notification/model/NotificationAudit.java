package com.rentflow.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_audit", indexes = {
    @Index(name = "idx_notifaudit_tenant", columnList = "tenantId"),
    @Index(name = "idx_notifaudit_notif", columnList = "notificationId")
})
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    private UUID notificationId;

    @Column(nullable = false)
    private String action; // e.g. NOTIFICATION_CREATED, NOTIFICATION_SENT, NOTIFICATION_FAILED, NOTIFICATION_READ, TEMPLATE_CREATED, PREFERENCE_UPDATED

    private String performedBy;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    public NotificationAudit() {}

    public NotificationAudit(String tenantId, UUID notificationId, String action, String performedBy, String details) {
        this.tenantId = tenantId;
        this.notificationId = notificationId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getNotificationId() { return notificationId; }
    public void setNotificationId(UUID notificationId) { this.notificationId = notificationId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
