package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.LoadListStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoadListDTO {
    private UUID id;
    private String tenantId;
    private String loadListNumber;
    private UUID warehouseOrderId;
    private String warehouseOrderNumber;
    private UUID deliveryId;
    private String deliveryNumber;
    private UUID bookingId;
    private String bookingNumber;
    private String customerName;
    private String eventName;
    private UUID vehicleId;
    private String vehicleCode;
    private String vehicleName;
    private double vehicleCapacityWeightKg;
    private double estimatedLoadWeightKg;
    private boolean capacityWarning;
    private String capacityWarningMessage;
    private UUID driverId;
    private String driverName;
    private LoadListStatus status;
    private String assignedTo;
    private double progressPercentage;
    private int totalItems;
    private int loadedItems;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime handedOffAt;
    private String handedOffToDriverName;
    private String driverNotes;
    private LocalDateTime createdAt;
    private List<LoadListItemDTO> items = new ArrayList<>();
    private List<String> containers = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getLoadListNumber() { return loadListNumber; }
    public void setLoadListNumber(String loadListNumber) { this.loadListNumber = loadListNumber; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public String getWarehouseOrderNumber() { return warehouseOrderNumber; }
    public void setWarehouseOrderNumber(String warehouseOrderNumber) { this.warehouseOrderNumber = warehouseOrderNumber; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleCode() { return vehicleCode; }
    public void setVehicleCode(String vehicleCode) { this.vehicleCode = vehicleCode; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public double getVehicleCapacityWeightKg() { return vehicleCapacityWeightKg; }
    public void setVehicleCapacityWeightKg(double vehicleCapacityWeightKg) { this.vehicleCapacityWeightKg = vehicleCapacityWeightKg; }

    public double getEstimatedLoadWeightKg() { return estimatedLoadWeightKg; }
    public void setEstimatedLoadWeightKg(double estimatedLoadWeightKg) { this.estimatedLoadWeightKg = estimatedLoadWeightKg; }

    public boolean isCapacityWarning() { return capacityWarning; }
    public void setCapacityWarning(boolean capacityWarning) { this.capacityWarning = capacityWarning; }

    public String getCapacityWarningMessage() { return capacityWarningMessage; }
    public void setCapacityWarningMessage(String capacityWarningMessage) { this.capacityWarningMessage = capacityWarningMessage; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public LoadListStatus getStatus() { return status; }
    public void setStatus(LoadListStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getLoadedItems() { return loadedItems; }
    public void setLoadedItems(int loadedItems) { this.loadedItems = loadedItems; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getHandedOffAt() { return handedOffAt; }
    public void setHandedOffAt(LocalDateTime handedOffAt) { this.handedOffAt = handedOffAt; }

    public String getHandedOffToDriverName() { return handedOffToDriverName; }
    public void setHandedOffToDriverName(String handedOffToDriverName) { this.handedOffToDriverName = handedOffToDriverName; }

    public String getDriverNotes() { return driverNotes; }
    public void setDriverNotes(String driverNotes) { this.driverNotes = driverNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<LoadListItemDTO> getItems() { return items; }
    public void setItems(List<LoadListItemDTO> items) { this.items = items; }

    public List<String> getContainers() { return containers; }
    public void setContainers(List<String> containers) { this.containers = containers; }
}
