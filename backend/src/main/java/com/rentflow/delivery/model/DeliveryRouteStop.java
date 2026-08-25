package com.rentflow.delivery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_route_stops", indexes = {
    @Index(name = "idx_stop_route", columnList = "routeId"),
    @Index(name = "idx_stop_delivery", columnList = "deliveryId")
})
public class DeliveryRouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID routeId;

    @Column(nullable = false)
    private UUID deliveryId;

    @Column(nullable = false)
    private Integer sequenceNumber;

    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(LocalDateTime estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public LocalDateTime getActualArrival() { return actualArrival; }
    public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
}
