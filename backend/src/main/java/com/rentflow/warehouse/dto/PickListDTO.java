package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.PickListStatus;
import com.rentflow.warehouse.model.WarehouseOrderPriority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PickListDTO {
    private UUID id;
    private String tenantId;
    private String pickListNumber;
    private UUID warehouseOrderId;
    private String warehouseOrderNumber;
    private UUID bookingId;
    private String bookingNumber;
    private String customerName;
    private String eventName;
    private LocalDateTime deliveryDate;
    private UUID warehouseId;
    private String warehouseName;
    private PickListStatus status;
    private WarehouseOrderPriority priority;
    private String assignedTo;
    private double progressPercentage;
    private int totalItems;
    private int pickedItems;
    private int blockingExceptionsCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PickListItemDTO> items = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPickListNumber() { return pickListNumber; }
    public void setPickListNumber(String pickListNumber) { this.pickListNumber = pickListNumber; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public String getWarehouseOrderNumber() { return warehouseOrderNumber; }
    public void setWarehouseOrderNumber(String warehouseOrderNumber) { this.warehouseOrderNumber = warehouseOrderNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public PickListStatus getStatus() { return status; }
    public void setStatus(PickListStatus status) { this.status = status; }

    public WarehouseOrderPriority getPriority() { return priority; }
    public void setPriority(WarehouseOrderPriority priority) { this.priority = priority; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getPickedItems() { return pickedItems; }
    public void setPickedItems(int pickedItems) { this.pickedItems = pickedItems; }

    public int getBlockingExceptionsCount() { return blockingExceptionsCount; }
    public void setBlockingExceptionsCount(int blockingExceptionsCount) { this.blockingExceptionsCount = blockingExceptionsCount; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PickListItemDTO> getItems() { return items; }
    public void setItems(List<PickListItemDTO> items) { this.items = items; }
}
