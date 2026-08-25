package com.rentflow.delivery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_audits", indexes = {
    @Index(name = "idx_del_audit_tenant", columnList = "tenantId"),
    @Index(name = "idx_del_audit_delivery", columnList = "deliveryId")
})
public class DeliveryAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    private UUID deliveryId;
    private UUID routeId;

    @Column(nullable = false)
    private String action;

    private String performedBy;

    @Column(length = 2000)
    private String details;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
