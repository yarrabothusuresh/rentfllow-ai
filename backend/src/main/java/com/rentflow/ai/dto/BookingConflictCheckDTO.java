package com.rentflow.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingConflictCheckDTO {
    private UUID bookingId; // Optional (null for new booking)
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID warehouseId;
    private UUID driverId;
    private UUID vehicleId;
    private LocalDateTime deliveryStart;
    private LocalDateTime deliveryEnd;
    private LocalDateTime pickupStart;
    private LocalDateTime pickupEnd;
    private List<Item> items;

    public BookingConflictCheckDTO() {}

    public static class Item {
        private UUID productId;
        private int quantity;

        public Item() {}
        public Item(UUID productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public LocalDateTime getDeliveryStart() { return deliveryStart; }
    public void setDeliveryStart(LocalDateTime deliveryStart) { this.deliveryStart = deliveryStart; }

    public LocalDateTime getDeliveryEnd() { return deliveryEnd; }
    public void setDeliveryEnd(LocalDateTime deliveryEnd) { this.deliveryEnd = deliveryEnd; }

    public LocalDateTime getPickupStart() { return pickupStart; }
    public void setPickupStart(LocalDateTime pickupStart) { this.pickupStart = pickupStart; }

    public LocalDateTime getPickupEnd() { return pickupEnd; }
    public void setPickupEnd(LocalDateTime pickupEnd) { this.pickupEnd = pickupEnd; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
