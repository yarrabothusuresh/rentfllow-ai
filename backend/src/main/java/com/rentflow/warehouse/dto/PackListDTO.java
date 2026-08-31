package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.PackListStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackListDTO {
    private UUID id;
    private String tenantId;
    private String packListNumber;
    private UUID warehouseOrderId;
    private String warehouseOrderNumber;
    private UUID bookingId;
    private String bookingNumber;
    private String customerName;
    private String eventName;
    private PackListStatus status;
    private String assignedTo;
    private double progressPercentage;
    private int totalItems;
    private int packedItems;
    private int containerCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private List<PackListItemDTO> items = new ArrayList<>();
    private List<KitDefinitionDTO> kits = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPackListNumber() { return packListNumber; }
    public void setPackListNumber(String packListNumber) { this.packListNumber = packListNumber; }

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

    public PackListStatus getStatus() { return status; }
    public void setStatus(PackListStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getPackedItems() { return packedItems; }
    public void setPackedItems(int packedItems) { this.packedItems = packedItems; }

    public int getContainerCount() { return containerCount; }
    public void setContainerCount(int containerCount) { this.containerCount = containerCount; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<PackListItemDTO> getItems() { return items; }
    public void setItems(List<PackListItemDTO> items) { this.items = items; }

    public List<KitDefinitionDTO> getKits() { return kits; }
    public void setKits(List<KitDefinitionDTO> kits) { this.kits = kits; }
}
