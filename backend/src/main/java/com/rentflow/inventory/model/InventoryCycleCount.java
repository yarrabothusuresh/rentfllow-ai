package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_cycle_counts", indexes = {
    @Index(name = "idx_cc_tenant", columnList = "tenantId"),
    @Index(name = "idx_cc_warehouse", columnList = "warehouseId"),
    @Index(name = "idx_cc_status", columnList = "status")
})
public class InventoryCycleCount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String countNumber;

    @Column(nullable = false)
    private UUID warehouseId;

    private UUID categoryId;

    @Column(nullable = false)
    private LocalDateTime countDate;

    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleCountStatus status = CycleCountStatus.PLANNED;

    private String approvedBy;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryCycleCount() {}

    public InventoryCycleCount(UUID id, String tenantId, String countNumber, UUID warehouseId,
                               UUID categoryId, LocalDateTime countDate, String assignedTo, String notes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.countNumber = countNumber;
        this.warehouseId = warehouseId;
        this.categoryId = categoryId;
        this.countDate = countDate != null ? countDate : LocalDateTime.now();
        this.assignedTo = assignedTo;
        this.status = CycleCountStatus.PLANNED;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getCountNumber() { return countNumber; }
    public void setCountNumber(String countNumber) { this.countNumber = countNumber; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getCountDate() { return countDate; }
    public void setCountDate(LocalDateTime countDate) { this.countDate = countDate; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public CycleCountStatus getStatus() { return status; }
    public void setStatus(CycleCountStatus status) { this.status = status; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
