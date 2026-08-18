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
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
