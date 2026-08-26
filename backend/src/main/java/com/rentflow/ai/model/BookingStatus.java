package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    DEPOSIT_PENDING,
    PARTIALLY_PAID,
    PAID,
    READY_FOR_FULFILLMENT,
    IN_PROGRESS,
    DELIVERED,
    READY_FOR_PICKUP,
    PICKED_UP,
    INSPECTING,
    RETURNED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
