package com.rentflow.warehouse.dto;

import java.util.UUID;

public class ProposeSubstitutionRequestDTO {
    private UUID warehouseOrderId;
    private UUID originalProductId;
    private UUID replacementProductId;
    private int originalQuantity;
    private int replacementQuantity;
    private String reason;

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getOriginalProductId() { return originalProductId; }
    public void setOriginalProductId(UUID originalProductId) { this.originalProductId = originalProductId; }

    public UUID getReplacementProductId() { return replacementProductId; }
    public void setReplacementProductId(UUID replacementProductId) { this.replacementProductId = replacementProductId; }

    public int getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(int originalQuantity) { this.originalQuantity = originalQuantity; }

    public int getReplacementQuantity() { return replacementQuantity; }
    public void setReplacementQuantity(int replacementQuantity) { this.replacementQuantity = replacementQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
