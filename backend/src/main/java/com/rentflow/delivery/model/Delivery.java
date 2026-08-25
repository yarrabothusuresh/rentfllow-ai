package com.rentflow.delivery.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_del_tenant", columnList = "tenantId"),
    @Index(name = "idx_del_wh_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_del_booking", columnList = "bookingId"),
    @Index(name = "idx_del_customer", columnList = "customerId"),
    @Index(name = "idx_del_driver", columnList = "driverId"),
    @Index(name = "idx_del_vehicle", columnList = "vehicleId"),
    @Index(name = "idx_del_date", columnList = "scheduledDate"),
    @Index(name = "idx_del_status", columnList = "status")
})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String deliveryNumber;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType deliveryType = DeliveryType.DELIVERY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryPriority priority = DeliveryPriority.NORMAL;

    private LocalDate scheduledDate;
    private String scheduledStartTime;
    private String scheduledEndTime;

    @Column(length = 1000)
    private String deliveryAddressSnapshot;

    private Double latitude;
    private Double longitude;

    private UUID driverId;
    private UUID vehicleId;

    private Integer sequenceNumber = 1;
    private Double estimatedDistance;
    private Integer estimatedDuration;

    private LocalDateTime actualStartTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualCompletionTime;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String customerNotes;

    private Boolean setupRequired = false;
    private Integer setupDurationMinutes = 0;

    private String failureReason;
    @Column(length = 2000)
    private String failureNotes;

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

    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public DeliveryType getDeliveryType() { return deliveryType; }
    public void setDeliveryType(DeliveryType deliveryType) { this.deliveryType = deliveryType; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public DeliveryPriority getPriority() { return priority; }
    public void setPriority(DeliveryPriority priority) { this.priority = priority; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(String scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public String getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(String scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }

    public String getDeliveryAddressSnapshot() { return deliveryAddressSnapshot; }
    public void setDeliveryAddressSnapshot(String deliveryAddressSnapshot) { this.deliveryAddressSnapshot = deliveryAddressSnapshot; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Double getEstimatedDistance() { return estimatedDistance; }
    public void setEstimatedDistance(Double estimatedDistance) { this.estimatedDistance = estimatedDistance; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }

    public LocalDateTime getActualCompletionTime() { return actualCompletionTime; }
    public void setActualCompletionTime(LocalDateTime actualCompletionTime) { this.actualCompletionTime = actualCompletionTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public Boolean getSetupRequired() { return setupRequired; }
    public void setSetupRequired(Boolean setupRequired) { this.setupRequired = setupRequired; }

    public Integer getSetupDurationMinutes() { return setupDurationMinutes; }
    public void setSetupDurationMinutes(Integer setupDurationMinutes) { this.setupDurationMinutes = setupDurationMinutes; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getFailureNotes() { return failureNotes; }
    public void setFailureNotes(String failureNotes) { this.failureNotes = failureNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
