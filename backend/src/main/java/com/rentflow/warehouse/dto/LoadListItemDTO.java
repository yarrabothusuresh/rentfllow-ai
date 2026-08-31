package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.LoadListItemStatus;
import java.util.UUID;

public class LoadListItemDTO {
    private UUID id;
    private UUID loadListId;
    private UUID productId;
    private UUID inventoryItemId;
    private UUID containerId;
    private String productName;
    private String containerCode;
    private int requiredQuantity;
    private int loadedQuantity;
    private int remainingQuantity;
    private LoadListItemStatus status;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLoadListId() { return loadListId; }
    public void setLoadListId(UUID loadListId) { this.loadListId = loadListId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getContainerId() { return containerId; }
    public void setContainerId(UUID containerId) { this.containerId = containerId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getContainerCode() { return containerCode; }
    public void setContainerCode(String containerCode) { this.containerCode = containerCode; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getLoadedQuantity() { return loadedQuantity; }
    public void setLoadedQuantity(int loadedQuantity) { this.loadedQuantity = loadedQuantity; }

    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public LoadListItemStatus getStatus() { return status; }
    public void setStatus(LoadListItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
