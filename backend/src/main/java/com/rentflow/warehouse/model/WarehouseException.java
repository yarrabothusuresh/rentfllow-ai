package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouse_exceptions", indexes = {
    @Index(name = "idx_wh_exc_tenant", columnList = "tenantId"),
    @Index(name = "idx_wh_exc_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_wh_exc_pick", columnList = "pickListId"),
    @Index(name = "idx_wh_exc_pack", columnList = "packListId"),
    @Index(name = "idx_wh_exc_load", columnList = "loadListId"),
    @Index(name = "idx_wh_exc_status", columnList = "status"),
    @Index(name = "idx_wh_exc_severity", columnList = "severity"),
    @Index(name = "idx_wh_exc_type", columnList = "type")
})
public class WarehouseException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    private UUID pickListId;
    private UUID packListId;
    private UUID loadListId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseExceptionType type = WarehouseExceptionType.SHORTAGE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseExceptionSeverity severity = WarehouseExceptionSeverity.BLOCKING;

    private UUID productId;
    private UUID inventoryItemId;
    private String productNameSnapshot;
    private String assetCodeSnapshot;

    private int quantity = 0;

    @Column(length = 2000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseExceptionStatus status = WarehouseExceptionStatus.OPEN;

    private String reportedBy;
    private String assignedTo;
    private String resolvedBy;

    @Enumerated(EnumType.STRING)
    private WarehouseExceptionResolution resolution;

    @Column(length = 2000)
    private String resolutionNotes;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = WarehouseExceptionStatus.OPEN;
        if (severity == null) severity = WarehouseExceptionSeverity.BLOCKING;
        if (type == null) type = WarehouseExceptionType.SHORTAGE;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getPickListId() { return pickListId; }
    public void setPickListId(UUID pickListId) { this.pickListId = pickListId; }

    public UUID getPackListId() { return packListId; }
    public void setPackListId(UUID packListId) { this.packListId = packListId; }

    public UUID getLoadListId() { return loadListId; }
    public void setLoadListId(UUID loadListId) { this.loadListId = loadListId; }

    public WarehouseExceptionType getType() { return type; }
    public void setType(WarehouseExceptionType type) { this.type = type; }

    public WarehouseExceptionSeverity getSeverity() { return severity; }
    public void setSeverity(WarehouseExceptionSeverity severity) { this.severity = severity; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getAssetCodeSnapshot() { return assetCodeSnapshot; }
    public void setAssetCodeSnapshot(String assetCodeSnapshot) { this.assetCodeSnapshot = assetCodeSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WarehouseExceptionStatus getStatus() { return status; }
    public void setStatus(WarehouseExceptionStatus status) { this.status = status; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public WarehouseExceptionResolution getResolution() { return resolution; }
    public void setResolution(WarehouseExceptionResolution resolution) { this.resolution = resolution; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
