package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ReservationStatus {
    PENDING,
    RESERVED,
    RELEASED,
    CANCELLED
}
