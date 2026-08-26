package com.rentflow.returns.dto;

import com.rentflow.returns.model.ReturnOrderStatus;
import com.rentflow.returns.model.ReturnPriority;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReturnOrderDTO {
    private UUID id;
    private String tenantId;
    private String returnNumber;
    private UUID bookingId;
    private String bookingNumber;
    private UUID deliveryId;
    private UUID customerId;
    private String customerName;
    private UUID eventId;
    private String eventName;
    private ReturnOrderStatus status;
    private ReturnPriority priority;
    private LocalDate scheduledDate;
    private String scheduledStartTime;
    private String scheduledEndTime;
    private String pickupAddressSnapshot;
    private UUID driverId;
    private String driverName;
    private UUID vehicleId;
    private String vehicleNumber;
    private String notes;
    private LocalDateTime actualPickupStartTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualInspectionTime;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReturnOrderItemDTO> items;
    private int totalExpectedItems;
    private int totalReceivedItems;
    private int totalMissingItems;
    private int totalDamagedItems;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public ReturnOrderStatus getStatus() { return status; }
    public void setStatus(ReturnOrderStatus status) { this.status = status; }

    public ReturnPriority getPriority() { return priority; }
    public void setPriority(ReturnPriority priority) { this.priority = priority; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(String scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public String getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(String scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }

    public String getPickupAddressSnapshot() { return pickupAddressSnapshot; }
    public void setPickupAddressSnapshot(String pickupAddressSnapshot) { this.pickupAddressSnapshot = pickupAddressSnapshot; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getActualPickupStartTime() { return actualPickupStartTime; }
    public void setActualPickupStartTime(LocalDateTime actualPickupStartTime) { this.actualPickupStartTime = actualPickupStartTime; }

    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }

    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }

    public LocalDateTime getActualCheckInTime() { return actualCheckInTime; }
    public void setActualCheckInTime(LocalDateTime actualCheckInTime) { this.actualCheckInTime = actualCheckInTime; }

    public LocalDateTime getActualInspectionTime() { return actualInspectionTime; }
    public void setActualInspectionTime(LocalDateTime actualInspectionTime) { this.actualInspectionTime = actualInspectionTime; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ReturnOrderItemDTO> getItems() { return items; }
    public void setItems(List<ReturnOrderItemDTO> items) { this.items = items; }

    public int getTotalExpectedItems() { return totalExpectedItems; }
    public void setTotalExpectedItems(int totalExpectedItems) { this.totalExpectedItems = totalExpectedItems; }

    public int getTotalReceivedItems() { return totalReceivedItems; }
    public void setTotalReceivedItems(int totalReceivedItems) { this.totalReceivedItems = totalReceivedItems; }

    public int getTotalMissingItems() { return totalMissingItems; }
    public void setTotalMissingItems(int totalMissingItems) { this.totalMissingItems = totalMissingItems; }

    public int getTotalDamagedItems() { return totalDamagedItems; }
    public void setTotalDamagedItems(int totalDamagedItems) { this.totalDamagedItems = totalDamagedItems; }
}
