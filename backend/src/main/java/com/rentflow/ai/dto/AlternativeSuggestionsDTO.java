package com.rentflow.ai.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlternativeSuggestionsDTO {
    private UUID productId;
    private String productName;
    private int requestedQuantity;
    private List<DateOption> alternativeDates = new ArrayList<>();
    private List<ProductOption> alternativeProducts = new ArrayList<>();

    public AlternativeSuggestionsDTO() {}

    public static class DateOption {
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private int availableQuantity;

        public DateOption() {}
        public DateOption(LocalDateTime startDateTime, LocalDateTime endDateTime, int availableQuantity) {
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.availableQuantity = availableQuantity;
        }

        public LocalDateTime getStartDateTime() { return startDateTime; }
        public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

        public LocalDateTime getEndDateTime() { return endDateTime; }
        public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    }

    public static class ProductOption {
        private UUID productId;
        private String productName;
        private String sku;
        private int availableQuantity;

        public ProductOption() {}
        public ProductOption(UUID productId, String productName, String sku, int availableQuantity) {
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

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public List<DateOption> getAlternativeDates() { return alternativeDates; }
    public void setAlternativeDates(List<DateOption> alternativeDates) { this.alternativeDates = alternativeDates; }

    public List<ProductOption> getAlternativeProducts() { return alternativeProducts; }
    public void setAlternativeProducts(List<ProductOption> alternativeProducts) { this.alternativeProducts = alternativeProducts; }
}
