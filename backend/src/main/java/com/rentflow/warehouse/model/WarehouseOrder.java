package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouse_orders", indexes = {
    @Index(name = "idx_wh_order_tenant", columnList = "tenantId"),
    @Index(name = "idx_wh_order_booking", columnList = "bookingId"),
    @Index(name = "idx_wh_order_event", columnList = "eventId"),
    @Index(name = "idx_wh_order_customer", columnList = "customerId"),
    @Index(name = "idx_wh_order_status", columnList = "status"),
    @Index(name = "idx_wh_order_priority", columnList = "priority")
})
public class WarehouseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    private LocalDateTime scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseOrderPriority priority = WarehouseOrderPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseOrderStatus status = WarehouseOrderStatus.PENDING;

    private String assignedTo;

    @Column(length = 2000)
    private String notes;

    private String createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (priority == null) priority = WarehouseOrderPriority.NORMAL;
        if (status == null) status = WarehouseOrderStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public WarehouseOrderPriority getPriority() { return priority; }
    public void setPriority(WarehouseOrderPriority priority) { this.priority = priority; }

    public WarehouseOrderStatus getStatus() { return status; }
    public void setStatus(WarehouseOrderStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
