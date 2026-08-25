package com.rentflow.delivery.dto;

import java.util.UUID;

public class AssignDriverDTO {
    private UUID driverId;

    public AssignDriverDTO() {}
    public AssignDriverDTO(UUID driverId) { this.driverId = driverId; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
}
