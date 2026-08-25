package com.rentflow.delivery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_veh_tenant", columnList = "tenantId"),
    @Index(name = "idx_veh_status", columnList = "status")
})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String name;

    private String type; // e.g. Van, Box Truck, Trailer
    private Integer capacity = 500; // total equipment capacity units

    @Column(nullable = false)
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
