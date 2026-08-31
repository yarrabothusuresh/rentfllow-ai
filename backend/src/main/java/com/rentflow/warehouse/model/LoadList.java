package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "load_lists", indexes = {
    @Index(name = "idx_load_list_tenant", columnList = "tenantId"),
    @Index(name = "idx_load_list_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_load_list_delivery", columnList = "deliveryId"),
    @Index(name = "idx_load_list_vehicle", columnList = "vehicleId"),
    @Index(name = "idx_load_list_driver", columnList = "driverId"),
    @Index(name = "idx_load_list_number", columnList = "loadListNumber"),
    @Index(name = "idx_load_list_status", columnList = "status")
})
public class LoadList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String loadListNumber;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    private UUID deliveryId;
    private UUID vehicleId;
    private UUID driverId;

    private String vehicleCodeSnapshot;
    private String driverNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadListStatus status = LoadListStatus.PENDING;

    private String assignedTo;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime verifiedAt;
    private String verifiedBy;

    private LocalDateTime handedOffAt;
    private String handedOffToDriverName;
    private String driverNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = LoadListStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getLoadListNumber() { return loadListNumber; }
    public void setLoadListNumber(String loadListNumber) { this.loadListNumber = loadListNumber; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getVehicleCodeSnapshot() { return vehicleCodeSnapshot; }
    public void setVehicleCodeSnapshot(String vehicleCodeSnapshot) { this.vehicleCodeSnapshot = vehicleCodeSnapshot; }

    public String getDriverNameSnapshot() { return driverNameSnapshot; }
    public void setDriverNameSnapshot(String driverNameSnapshot) { this.driverNameSnapshot = driverNameSnapshot; }

    public LoadListStatus getStatus() { return status; }
    public void setStatus(LoadListStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

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

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
