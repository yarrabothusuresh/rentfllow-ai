package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.PickListItemStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class PickListItemDTO {
    private UUID id;
    private UUID pickListId;
    private UUID bookingItemId;
    private UUID productId;
    private UUID inventoryItemId;
    private UUID warehouseLocationId;
    private String productName;
    private String productSku;
    private String locationCode;
    private String imageUrl;
    private int requiredQuantity;
    private int pickedQuantity;
    private int shortQuantity;
    private int damagedQuantity;
    private int substitutedQuantity;
    private int remainingQuantity;
    private PickListItemStatus status;
    private int sequenceNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPickListId() { return pickListId; }
    public void setPickListId(UUID pickListId) { this.pickListId = pickListId; }

    public UUID getBookingItemId() { return bookingItemId; }
    public void setBookingItemId(UUID bookingItemId) { this.bookingItemId = bookingItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getWarehouseLocationId() { return warehouseLocationId; }
    public void setWarehouseLocationId(UUID warehouseLocationId) { this.warehouseLocationId = warehouseLocationId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getPickedQuantity() { return pickedQuantity; }
    public void setPickedQuantity(int pickedQuantity) { this.pickedQuantity = pickedQuantity; }

    public int getShortQuantity() { return shortQuantity; }
    public void setShortQuantity(int shortQuantity) { this.shortQuantity = shortQuantity; }

    public int getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }

    public int getSubstitutedQuantity() { return substitutedQuantity; }
    public void setSubstitutedQuantity(int substitutedQuantity) { this.substitutedQuantity = substitutedQuantity; }

    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public PickListItemStatus getStatus() { return status; }
    public void setStatus(PickListItemStatus status) { this.status = status; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
