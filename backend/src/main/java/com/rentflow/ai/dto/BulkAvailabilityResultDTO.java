package com.rentflow.ai.dto;

import java.util.List;
import java.util.UUID;

public class BulkAvailabilityResultDTO {

    private boolean available;
    private List<ItemResult> items;

    public static class ItemResult {
        private UUID productId;
        private String productName;
        private String sku;
        private int requestedQuantity;
        private int availableQuantity;
        private int shortageQuantity;
        private boolean available;

        public ItemResult() {}

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public int getRequestedQuantity() { return requestedQuantity; }
        public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

        public int getShortageQuantity() { return shortageQuantity; }
        public void setShortageQuantity(int shortageQuantity) { this.shortageQuantity = shortageQuantity; }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    public BulkAvailabilityResultDTO() {}

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<ItemResult> getItems() { return items; }
    public void setItems(List<ItemResult> items) { this.items = items; }
}
