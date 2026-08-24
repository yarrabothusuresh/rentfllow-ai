package com.rentflow.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InventoryConflictDTO {

    private UUID productId;
    private String productName;
    private String sku;
    private UUID bookingId;
    private String bookingNumber;
    private UUID eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private int requestedQuantity;
    private int availableQuantity;
    private int shortageQuantity;
    private String priority;
    private List<AlternativeProductDTO> suggestedAlternatives;

    public static class AlternativeProductDTO {
        private UUID productId;
        private String productName;
        private String sku;
        private int availableQuantity;

        public AlternativeProductDTO() {}

        public AlternativeProductDTO(UUID productId, String productName, String sku, int availableQuantity) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.availableQuantity = availableQuantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    }

    public InventoryConflictDTO() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public int getShortageQuantity() { return shortageQuantity; }
    public void setShortageQuantity(int shortageQuantity) { this.shortageQuantity = shortageQuantity; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public List<AlternativeProductDTO> getSuggestedAlternatives() { return suggestedAlternatives; }
    public void setSuggestedAlternatives(List<AlternativeProductDTO> suggestedAlternatives) { this.suggestedAlternatives = suggestedAlternatives; }
}
