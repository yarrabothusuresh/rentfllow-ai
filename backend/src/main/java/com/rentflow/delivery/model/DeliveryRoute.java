package com.rentflow.delivery.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_routes", indexes = {
    @Index(name = "idx_route_tenant", columnList = "tenantId"),
    @Index(name = "idx_route_date", columnList = "routeDate"),
    @Index(name = "idx_route_driver", columnList = "driverId"),
    @Index(name = "idx_route_vehicle", columnList = "vehicleId")
})
public class DeliveryRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String routeNumber;

    private UUID driverId;
    private UUID vehicleId;

    @Column(nullable = false)
    private LocalDate routeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryRouteStatus status = DeliveryRouteStatus.PLANNED;

    private Integer totalDeliveries = 0;
    private Double estimatedDistance = 0.0;
    private Integer estimatedDuration = 0;

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

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
