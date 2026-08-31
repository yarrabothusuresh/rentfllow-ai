package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pack_lists", indexes = {
    @Index(name = "idx_pack_list_tenant", columnList = "tenantId"),
    @Index(name = "idx_pack_list_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_pack_list_number", columnList = "packListNumber"),
    @Index(name = "idx_pack_list_status", columnList = "status"),
    @Index(name = "idx_pack_list_assigned", columnList = "assignedTo")
})
public class PackList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String packListNumber;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackListStatus status = PackListStatus.PENDING;

    private String assignedTo;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PackListStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPackListNumber() { return packListNumber; }
    public void setPackListNumber(String packListNumber) { this.packListNumber = packListNumber; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public PackListStatus getStatus() { return status; }
    public void setStatus(PackListStatus status) { this.status = status; }

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
