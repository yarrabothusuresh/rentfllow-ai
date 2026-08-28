package com.rentflow.portal.dto;

import java.util.UUID;

public class PublicAvailabilityResponseDTO {
    private UUID productId;
    private int requestedQuantity;
    private int availableQuantity;
    private boolean available;

    public PublicAvailabilityResponseDTO() {}

    public PublicAvailabilityResponseDTO(UUID productId, int requestedQuantity, int availableQuantity, boolean available) {
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.available = available;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
