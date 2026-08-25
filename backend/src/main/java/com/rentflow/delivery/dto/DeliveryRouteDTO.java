package com.rentflow.delivery.dto;

import com.rentflow.delivery.model.DeliveryRouteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DeliveryRouteDTO {
    private UUID id;
    private String tenantId;
    private String routeNumber;
    private UUID driverId;
    private String driverName;
    private UUID vehicleId;
    private String vehicleNumber;
    private LocalDate routeDate;
    private DeliveryRouteStatus status;
    private Integer totalDeliveries;
    private Double estimatedDistance;
    private Integer estimatedDuration;
    private List<DeliveryRouteStopDTO> stops;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public LocalDate getRouteDate() { return routeDate; }
    public void setRouteDate(LocalDate routeDate) { this.routeDate = routeDate; }
    public DeliveryRouteStatus getStatus() { return status; }
    public void setStatus(DeliveryRouteStatus status) { this.status = status; }
    public Integer getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    public Double getEstimatedDistance() { return estimatedDistance; }
    public void setEstimatedDistance(Double estimatedDistance) { this.estimatedDistance = estimatedDistance; }
    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    public List<DeliveryRouteStopDTO> getStops() { return stops; }
    public void setStops(List<DeliveryRouteStopDTO> stops) { this.stops = stops; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
