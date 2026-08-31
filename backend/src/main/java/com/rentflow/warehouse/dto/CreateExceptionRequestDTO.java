package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.WarehouseExceptionSeverity;
import com.rentflow.warehouse.model.WarehouseExceptionType;
import java.util.UUID;

public class CreateExceptionRequestDTO {
    private UUID warehouseOrderId;
    private UUID pickListId;
    private UUID packListId;
    private UUID loadListId;
    private WarehouseExceptionType type;
    private WarehouseExceptionSeverity severity;
    private UUID productId;
    private UUID inventoryItemId;
    private int quantity;
    private String description;

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

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
