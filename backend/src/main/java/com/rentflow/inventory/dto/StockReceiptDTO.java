package com.rentflow.inventory.dto;

import com.rentflow.inventory.model.AssetCondition;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class StockReceiptDTO {
    private UUID productId;
    private UUID warehouseId;
    private int quantity;
    private AssetCondition condition = AssetCondition.NEW;
    private String reference;
    private BigDecimal purchaseCost;
    private String notes;
    private List<String> serialNumbers; // For serialized receiving

    public StockReceiptDTO() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public AssetCondition getCondition() { return condition; }
    public void setCondition(AssetCondition condition) { this.condition = condition; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public BigDecimal getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getSerialNumbers() { return serialNumbers; }
    public void setSerialNumbers(List<String> serialNumbers) { this.serialNumbers = serialNumbers; }
}
