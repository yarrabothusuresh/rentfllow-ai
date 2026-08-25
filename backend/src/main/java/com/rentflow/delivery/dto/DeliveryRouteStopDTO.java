package com.rentflow.delivery.dto;

import com.rentflow.delivery.model.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryRouteStopDTO {
    private UUID id;
    private UUID routeId;
    private UUID deliveryId;
    private String deliveryNumber;
    private String customerName;
    private String deliveryAddress;
    private Integer sequenceNumber;
    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;
    private DeliveryStatus status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }
    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(LocalDateTime estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public LocalDateTime getActualArrival() { return actualArrival; }
    public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
}
