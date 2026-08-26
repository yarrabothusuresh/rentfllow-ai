package com.rentflow.returns.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_orders", indexes = {
    @Index(name = "idx_return_order_tenant", columnList = "tenantId"),
    @Index(name = "idx_return_order_booking", columnList = "bookingId"),
    @Index(name = "idx_return_order_status", columnList = "status"),
    @Index(name = "idx_return_order_driver", columnList = "driverId"),
    @Index(name = "idx_return_order_vehicle", columnList = "vehicleId"),
    @Index(name = "idx_return_order_date", columnList = "scheduledDate")
})
public class ReturnOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String returnNumber;

    @Column(nullable = false)
    private UUID bookingId;

    private UUID deliveryId;

    @Column(nullable = false)
    private UUID customerId;

    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnOrderStatus status = ReturnOrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnPriority priority = ReturnPriority.NORMAL;

    private LocalDate scheduledDate;
    private String scheduledStartTime;
    private String scheduledEndTime;

    @Column(length = 2000)
    private String pickupAddressSnapshot;

    private UUID driverId;
    private UUID vehicleId;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime actualPickupStartTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualInspectionTime;
    private LocalDateTime completedAt;

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

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public ReturnOrderStatus getStatus() { return status; }
    public void setStatus(ReturnOrderStatus status) { this.status = status; }

    public ReturnPriority getPriority() { return priority; }
    public void setPriority(ReturnPriority priority) { this.priority = priority; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(String scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public String getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(String scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }

    public String getPickupAddressSnapshot() { return pickupAddressSnapshot; }
    public void setPickupAddressSnapshot(String pickupAddressSnapshot) { this.pickupAddressSnapshot = pickupAddressSnapshot; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getActualPickupStartTime() { return actualPickupStartTime; }
    public void setActualPickupStartTime(LocalDateTime actualPickupStartTime) { this.actualPickupStartTime = actualPickupStartTime; }

    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }

    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }

    public LocalDateTime getActualCheckInTime() { return actualCheckInTime; }
    public void setActualCheckInTime(LocalDateTime actualCheckInTime) { this.actualCheckInTime = actualCheckInTime; }

    public LocalDateTime getActualInspectionTime() { return actualInspectionTime; }
    public void setActualInspectionTime(LocalDateTime actualInspectionTime) { this.actualInspectionTime = actualInspectionTime; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
