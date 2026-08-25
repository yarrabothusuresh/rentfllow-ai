package com.rentflow.delivery.dto;

import com.rentflow.delivery.model.VehicleStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class VehicleDTO {
    private UUID id;
    private String tenantId;
    private String vehicleNumber;
    private String name;
    private String type;
    private Integer capacity;
    private Boolean active;
    private VehicleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
