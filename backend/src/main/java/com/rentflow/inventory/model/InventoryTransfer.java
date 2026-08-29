package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_transfers", indexes = {
    @Index(name = "idx_transfer_tenant", columnList = "tenantId"),
    @Index(name = "idx_transfer_number", columnList = "transferNumber"),
    @Index(name = "idx_transfer_status", columnList = "status")
})
public class InventoryTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String transferNumber;

    @Column(nullable = false)
    private UUID fromWarehouseId;

    @Column(nullable = false)
    private UUID toWarehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.DRAFT;

    private String requestedBy;
    private String approvedBy;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryTransfer() {}

    public InventoryTransfer(UUID id, String tenantId, String transferNumber, UUID fromWarehouseId,
                             UUID toWarehouseId, TransferStatus status, String requestedBy, String notes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.transferNumber = transferNumber;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.status = status != null ? status : TransferStatus.DRAFT;
        this.requestedBy = requestedBy;
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

    public String getTransferNumber() { return transferNumber; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }

    public UUID getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(UUID fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public UUID getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(UUID toWarehouseId) { this.toWarehouseId = toWarehouseId; }

    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
