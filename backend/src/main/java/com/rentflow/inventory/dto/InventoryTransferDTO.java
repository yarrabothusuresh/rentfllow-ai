package com.rentflow.inventory.dto;

import com.rentflow.inventory.model.TransferStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InventoryTransferDTO {
    private UUID id;
    private String transferNumber;
    private UUID fromWarehouseId;
    private String fromWarehouseName;
    private UUID toWarehouseId;
    private String toWarehouseName;
    private TransferStatus status;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private String notes;
    private List<TransferItemDTO> items;
    private LocalDateTime createdAt;

    public static class TransferItemDTO {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productSku;
        private UUID inventoryItemId;
        private String assetCode;
        private int quantity;
        private int receivedQuantity;
        private String condition;
        private String notes;

        public TransferItemDTO() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }

        public UUID getInventoryItemId() { return inventoryItemId; }
        public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

        public String getAssetCode() { return assetCode; }
        public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public int getReceivedQuantity() { return receivedQuantity; }
        public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public InventoryTransferDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTransferNumber() { return transferNumber; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }

    public UUID getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(UUID fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public String getFromWarehouseName() { return fromWarehouseName; }
    public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }

    public UUID getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(UUID toWarehouseId) { this.toWarehouseId = toWarehouseId; }

    public String getToWarehouseName() { return toWarehouseName; }
    public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }

    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<TransferItemDTO> getItems() { return items; }
    public void setItems(List<TransferItemDTO> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
