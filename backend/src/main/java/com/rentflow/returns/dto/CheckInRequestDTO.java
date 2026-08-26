package com.rentflow.returns.dto;

import java.util.List;
import java.util.UUID;

public class CheckInRequestDTO {

    private List<ItemQuantity> items;

    public List<ItemQuantity> getItems() { return items; }
    public void setItems(List<ItemQuantity> items) { this.items = items; }

    public static class ItemQuantity {
        private UUID returnItemId;
        private int quantityReceived;

        public UUID getReturnItemId() { return returnItemId; }
        public void setReturnItemId(UUID returnItemId) { this.returnItemId = returnItemId; }

        public int getQuantityReceived() { return quantityReceived; }
        public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }
    }
}
