package com.rentflow.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AvailabilityResultDTO {
    private UUID productId;
    private String productName;
    private String sku;
    private int requestedQuantity;
    private int quantityOwned;
    private int quantityInMaintenance;
    private int quantityDamaged;
    private int quantityLost;
    private int quantityReserved;
    private int availableQuantity;
    private boolean available;
    private int shortage;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<InventoryReservationDTO> conflictingReservations;

    public AvailabilityResultDTO() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public int getQuantityOwned() { return quantityOwned; }
    public void setQuantityOwned(int quantityOwned) { this.quantityOwned = quantityOwned; }

    public int getQuantityInMaintenance() { return quantityInMaintenance; }
    public void setQuantityInMaintenance(int quantityInMaintenance) { this.quantityInMaintenance = quantityInMaintenance; }

    public int getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(int quantityDamaged) { this.quantityDamaged = quantityDamaged; }

    public int getQuantityLost() { return quantityLost; }
    public void setQuantityLost(int quantityLost) { this.quantityLost = quantityLost; }

    public int getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getShortage() { return shortage; }
    public void setShortage(int shortage) { this.shortage = shortage; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public List<InventoryReservationDTO> getConflictingReservations() { return conflictingReservations; }
    public void setConflictingReservations(List<InventoryReservationDTO> conflictingReservations) { this.conflictingReservations = conflictingReservations; }
}
