package com.rentflow.ai.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AvailabilityCheckRequest {
    private UUID productId;
    private int requestedQuantity;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public AvailabilityCheckRequest() {}

    public AvailabilityCheckRequest(UUID productId, int requestedQuantity, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
}
