package com.rentflow.inventory.dto;

import com.rentflow.inventory.model.MovementType;

import java.util.UUID;

public class StockAdjustmentDTO {
    private UUID productId;
    private UUID warehouseId;
    private UUID inventoryItemId;
    private MovementType type; // ADJUSTMENT_IN, ADJUSTMENT_OUT, DAMAGE, LOSS, RETIREMENT
    private int quantity = 1;
    private String reason;
    private String reference;
    private String notes;

    public StockAdjustmentDTO() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public MovementType getType() { return type; }
    public void setType(MovementType type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
