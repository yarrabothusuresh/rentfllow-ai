package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.WarehouseOrderItemStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class WarehouseOrderItemDTO {

    private UUID id;
    private UUID warehouseOrderId;
    private UUID bookingItemId;
    private UUID productId;
    private String productNameSnapshot;
    private String skuSnapshot;
    private String locationSnapshot;
    private int quantityRequired;
    private int quantityPicked;
    private int quantityPacked;
    private WarehouseOrderItemStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getBookingItemId() { return bookingItemId; }
    public void setBookingItemId(UUID bookingItemId) { this.bookingItemId = bookingItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public String getLocationSnapshot() { return locationSnapshot; }
    public void setLocationSnapshot(String locationSnapshot) { this.locationSnapshot = locationSnapshot; }

    public int getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(int quantityRequired) { this.quantityRequired = quantityRequired; }

    public int getQuantityPicked() { return quantityPicked; }
    public void setQuantityPicked(int quantityPicked) { this.quantityPicked = quantityPicked; }

    public int getQuantityPacked() { return quantityPacked; }
    public void setQuantityPacked(int quantityPacked) { this.quantityPacked = quantityPacked; }

    public WarehouseOrderItemStatus getStatus() { return status; }
    public void setStatus(WarehouseOrderItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
