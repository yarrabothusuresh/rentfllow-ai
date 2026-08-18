package com.rentflow.ai.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingUnavailableDTO {
    private String error = "BOOKING_UNAVAILABLE";
    private String message = "This quote can no longer be fulfilled because inventory availability has changed.";
    private List<ShortageItemDTO> items = new ArrayList<>();

    public static class ShortageItemDTO {
        private UUID productId;
        private String productName;
        private int requestedQuantity;
        private int availableQuantity;
        private int shortage;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;

        public ShortageItemDTO() {}

        public ShortageItemDTO(UUID productId, String productName, int requestedQuantity, int availableQuantity, int shortage, LocalDateTime startDateTime, LocalDateTime endDateTime) {
            this.productId = productId;
            this.productName = productName;
            this.requestedQuantity = requestedQuantity;
            this.availableQuantity = availableQuantity;
            this.shortage = shortage;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public int getRequestedQuantity() { return requestedQuantity; }
        public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

        public int getShortage() { return shortage; }
        public void setShortage(int shortage) { this.shortage = shortage; }

        public LocalDateTime getStartDateTime() { return startDateTime; }
        public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

        public LocalDateTime getEndDateTime() { return endDateTime; }
        public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<ShortageItemDTO> getItems() { return items; }
    public void setItems(List<ShortageItemDTO> items) { this.items = items; }
}
