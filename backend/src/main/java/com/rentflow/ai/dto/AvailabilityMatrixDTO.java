package com.rentflow.ai.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AvailabilityMatrixDTO {
    private List<LocalDate> dates = new ArrayList<>();
    private List<ProductMatrixRow> products = new ArrayList<>();

    public AvailabilityMatrixDTO() {}

    public List<LocalDate> getDates() { return dates; }
    public void setDates(List<LocalDate> dates) { this.dates = dates; }

    public List<ProductMatrixRow> getProducts() { return products; }
    public void setProducts(List<ProductMatrixRow> products) { this.products = products; }

    public static class ProductMatrixRow {
        private UUID productId;
        private String productName;
        private String sku;
        private int totalOwned;
        private List<Integer> availableByDate = new ArrayList<>();

        public ProductMatrixRow() {}

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public int getTotalOwned() { return totalOwned; }
        public void setTotalOwned(int totalOwned) { this.totalOwned = totalOwned; }

        public List<Integer> getAvailableByDate() { return availableByDate; }
        public void setAvailableByDate(List<Integer> availableByDate) { this.availableByDate = availableByDate; }
    }
}
