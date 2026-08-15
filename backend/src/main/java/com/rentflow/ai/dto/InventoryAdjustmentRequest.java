package com.rentflow.ai.dto;

import com.rentflow.ai.model.TransactionType;

public class InventoryAdjustmentRequest {
    private int quantity;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private TransactionType type;
    private String reason;

    public InventoryAdjustmentRequest() {}

    public InventoryAdjustmentRequest(int quantity, TransactionType type, String reason) {
        this.quantity = quantity;
        this.type = type;
        this.reason = reason;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
