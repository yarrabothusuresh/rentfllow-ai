package com.rentflow.integration.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ExternalAvailabilityDTO {
    private UUID productId;
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalQuantity;
    private int reservedQuantity;
    private int availableQuantity;
    private boolean isAvailable;

    public ExternalAvailabilityDTO() {}

    public ExternalAvailabilityDTO(UUID productId, String productName, LocalDate startDate,
                                   LocalDate endDate, int totalQuantity, int reservedQuantity,
                                   int availableQuantity, boolean isAvailable) {
        this.productId = productId;
        this.productName = productName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.isAvailable = isAvailable;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
