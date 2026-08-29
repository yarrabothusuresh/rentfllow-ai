package com.rentflow.inventory.dto;

import java.util.List;
import java.util.UUID;

public class CycleCountRequestDTO {
    private UUID warehouseId;
    private UUID categoryId;
    private String assignedTo;
    private String notes;
    private List<CountItemDTO> items;

    public static class CountItemDTO {
        private UUID productId;
        private UUID inventoryItemId;
        private int countedQuantity;
        private String reason;
        private String notes;

        public CountItemDTO() {}

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public UUID getInventoryItemId() { return inventoryItemId; }
        public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

        public int getCountedQuantity() { return countedQuantity; }
        public void setCountedQuantity(int countedQuantity) { this.countedQuantity = countedQuantity; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public CycleCountRequestDTO() {}

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<CountItemDTO> getItems() { return items; }
    public void setItems(List<CountItemDTO> items) { this.items = items; }
}
