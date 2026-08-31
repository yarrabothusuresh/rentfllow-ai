package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.PackListItemStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class PackListItemDTO {
    private UUID id;
    private UUID packListId;
    private UUID productId;
    private UUID inventoryItemId;
    private UUID containerId;
    private String productName;
    private String productSku;
    private String containerCode;
    private int requiredQuantity;
    private int packedQuantity;
    private int remainingQuantity;
    private PackListItemStatus status;
    private String notes;
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPackListId() { return packListId; }
    public void setPackListId(UUID packListId) { this.packListId = packListId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getContainerId() { return containerId; }
    public void setContainerId(UUID containerId) { this.containerId = containerId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getContainerCode() { return containerCode; }
    public void setContainerCode(String containerCode) { this.containerCode = containerCode; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getPackedQuantity() { return packedQuantity; }
    public void setPackedQuantity(int packedQuantity) { this.packedQuantity = packedQuantity; }

    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public PackListItemStatus getStatus() { return status; }
    public void setStatus(PackListItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
