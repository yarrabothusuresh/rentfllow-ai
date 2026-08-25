package com.rentflow.delivery.dto;

import java.util.UUID;

public class AssignVehicleDTO {
    private UUID vehicleId;

    public AssignVehicleDTO() {}
    public AssignVehicleDTO(UUID vehicleId) { this.vehicleId = vehicleId; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
}
