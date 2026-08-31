package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pick_lists", indexes = {
    @Index(name = "idx_pick_list_tenant", columnList = "tenantId"),
    @Index(name = "idx_pick_list_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_pick_list_number", columnList = "pickListNumber"),
    @Index(name = "idx_pick_list_status", columnList = "status"),
    @Index(name = "idx_pick_list_assigned", columnList = "assignedTo"),
    @Index(name = "idx_pick_list_priority", columnList = "priority")
})
public class PickList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String pickListNumber;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    private UUID warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickListStatus status = PickListStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseOrderPriority priority = WarehouseOrderPriority.NORMAL;

    private String assignedTo;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PickListStatus.PENDING;
        if (priority == null) priority = WarehouseOrderPriority.NORMAL;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPickListNumber() { return pickListNumber; }
    public void setPickListNumber(String pickListNumber) { this.pickListNumber = pickListNumber; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public PickListStatus getStatus() { return status; }
    public void setStatus(PickListStatus status) { this.status = status; }

    public WarehouseOrderPriority getPriority() { return priority; }
    public void setPriority(WarehouseOrderPriority priority) { this.priority = priority; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
