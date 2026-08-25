package com.rentflow.delivery.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum DeliveryStatus {
    PENDING,
    SCHEDULED,
    ASSIGNED,
    READY,
    OUT_FOR_DELIVERY,
    ARRIVED,
    SETUP_IN_PROGRESS,
    DELIVERED,
    CANCELLED,
    FAILED
}
