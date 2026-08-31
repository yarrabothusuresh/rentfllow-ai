package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.WarehouseExceptionResolution;
import com.rentflow.warehouse.model.WarehouseExceptionSeverity;
import com.rentflow.warehouse.model.WarehouseExceptionStatus;
import com.rentflow.warehouse.model.WarehouseExceptionType;
import java.time.LocalDateTime;
import java.util.UUID;

public class WarehouseExceptionDTO {
    private UUID id;
    private String tenantId;
    private UUID warehouseOrderId;
    private String warehouseOrderNumber;
    private UUID pickListId;
    private String pickListNumber;
    private UUID packListId;
    private UUID loadListId;
    private WarehouseExceptionType type;
    private WarehouseExceptionSeverity severity;
    private UUID productId;
    private String productName;
    private UUID inventoryItemId;
    private String assetCode;
    private int quantity;
    private String description;
    private WarehouseExceptionStatus status;
    private String reportedBy;
    private String assignedTo;
    private String resolvedBy;
    private WarehouseExceptionResolution resolution;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public String getWarehouseOrderNumber() { return warehouseOrderNumber; }
    public void setWarehouseOrderNumber(String warehouseOrderNumber) { this.warehouseOrderNumber = warehouseOrderNumber; }

    public UUID getPickListId() { return pickListId; }
    public void setPickListId(UUID pickListId) { this.pickListId = pickListId; }

    public String getPickListNumber() { return pickListNumber; }
    public void setPickListNumber(String pickListNumber) { this.pickListNumber = pickListNumber; }

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

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

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
