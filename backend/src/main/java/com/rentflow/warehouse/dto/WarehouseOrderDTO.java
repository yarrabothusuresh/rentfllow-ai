package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.WarehouseOrderPriority;
import com.rentflow.warehouse.model.WarehouseOrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarehouseOrderDTO {

    private UUID id;
    private String tenantId;
    private UUID bookingId;
    private String bookingNumber;
    private UUID eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private String venueName;
    private UUID customerId;
    private String customerName;
    private String orderNumber;
    private LocalDateTime scheduledDate;
    private WarehouseOrderPriority priority;
    private WarehouseOrderStatus status;
    private String customerFacingStatus;
    private String assignedTo;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private int totalQuantityRequired;
    private int totalQuantityPicked;
    private int totalQuantityPacked;
    private double pickingProgressPercentage;

    private List<WarehouseOrderItemDTO> items = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

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

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public WarehouseOrderPriority getPriority() { return priority; }
    public void setPriority(WarehouseOrderPriority priority) { this.priority = priority; }

    public WarehouseOrderStatus getStatus() { return status; }
    public void setStatus(WarehouseOrderStatus status) { this.status = status; }

    public String getCustomerFacingStatus() { return customerFacingStatus; }
    public void setCustomerFacingStatus(String customerFacingStatus) { this.customerFacingStatus = customerFacingStatus; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getTotalQuantityRequired() { return totalQuantityRequired; }
    public void setTotalQuantityRequired(int totalQuantityRequired) { this.totalQuantityRequired = totalQuantityRequired; }

    public int getTotalQuantityPicked() { return totalQuantityPicked; }
    public void setTotalQuantityPicked(int totalQuantityPicked) { this.totalQuantityPicked = totalQuantityPicked; }

    public int getTotalQuantityPacked() { return totalQuantityPacked; }
    public void setTotalQuantityPacked(int totalQuantityPacked) { this.totalQuantityPacked = totalQuantityPacked; }

    public double getPickingProgressPercentage() { return pickingProgressPercentage; }
    public void setPickingProgressPercentage(double pickingProgressPercentage) { this.pickingProgressPercentage = pickingProgressPercentage; }

    public List<WarehouseOrderItemDTO> getItems() { return items; }
    public void setItems(List<WarehouseOrderItemDTO> items) { this.items = items; }
}
