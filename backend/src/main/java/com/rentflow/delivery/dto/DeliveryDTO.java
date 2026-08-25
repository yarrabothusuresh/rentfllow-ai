package com.rentflow.delivery.dto;

import com.rentflow.delivery.model.DeliveryPriority;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.model.DeliveryType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DeliveryDTO {

    private UUID id;
    private String tenantId;
    private String deliveryNumber;
    private UUID bookingId;
    private String bookingNumber;
    private UUID warehouseOrderId;
    private String warehouseOrderNumber;
    private UUID customerId;
    private String customerName;
    private UUID eventId;
    private String eventName;
    private DeliveryType deliveryType;
    private DeliveryStatus status;
    private DeliveryPriority priority;
    private LocalDate scheduledDate;
    private String scheduledStartTime;
    private String scheduledEndTime;
    private String deliveryAddressSnapshot;
    private Double latitude;
    private Double longitude;
    private UUID driverId;
    private String driverName;
    private UUID vehicleId;
    private String vehicleNumber;
    private Integer sequenceNumber;
    private Double estimatedDistance;
    private Integer estimatedDuration;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualCompletionTime;
    private String notes;
    private String customerNotes;
    private Boolean setupRequired;
    private Integer setupDurationMinutes;
    private String failureReason;
    private String failureNotes;
    private List<DeliveryItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class DeliveryItemDTO {
        private UUID productId;
        private String sku;
        private String name;
        private Integer quantity;

        public DeliveryItemDTO() {}
        public DeliveryItemDTO(UUID productId, String sku, String name, Integer quantity) {
            this.productId = productId;
            this.sku = sku;
            this.name = name;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }
    public String getWarehouseOrderNumber() { return warehouseOrderNumber; }
    public void setWarehouseOrderNumber(String warehouseOrderNumber) { this.warehouseOrderNumber = warehouseOrderNumber; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public DeliveryType getDeliveryType() { return deliveryType; }
    public void setDeliveryType(DeliveryType deliveryType) { this.deliveryType = deliveryType; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public DeliveryPriority getPriority() { return priority; }
    public void setPriority(DeliveryPriority priority) { this.priority = priority; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(String scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    public String getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(String scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    public String getDeliveryAddressSnapshot() { return deliveryAddressSnapshot; }
    public void setDeliveryAddressSnapshot(String deliveryAddressSnapshot) { this.deliveryAddressSnapshot = deliveryAddressSnapshot; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public Double getEstimatedDistance() { return estimatedDistance; }
    public void setEstimatedDistance(Double estimatedDistance) { this.estimatedDistance = estimatedDistance; }
    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }
    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }
    public LocalDateTime getActualCompletionTime() { return actualCompletionTime; }
    public void setActualCompletionTime(LocalDateTime actualCompletionTime) { this.actualCompletionTime = actualCompletionTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
    public Boolean getSetupRequired() { return setupRequired; }
    public void setSetupRequired(Boolean setupRequired) { this.setupRequired = setupRequired; }
    public Integer getSetupDurationMinutes() { return setupDurationMinutes; }
    public void setSetupDurationMinutes(Integer setupDurationMinutes) { this.setupDurationMinutes = setupDurationMinutes; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getFailureNotes() { return failureNotes; }
    public void setFailureNotes(String failureNotes) { this.failureNotes = failureNotes; }
    public List<DeliveryItemDTO> getItems() { return items; }
    public void setItems(List<DeliveryItemDTO> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
