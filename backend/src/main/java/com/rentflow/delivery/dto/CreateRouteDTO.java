package com.rentflow.delivery.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateRouteDTO {
    private LocalDate date;
    private UUID driverId;
    private UUID vehicleId;
    private List<UUID> deliveryIds;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public List<UUID> getDeliveryIds() { return deliveryIds; }
    public void setDeliveryIds(List<UUID> deliveryIds) { this.deliveryIds = deliveryIds; }
}
