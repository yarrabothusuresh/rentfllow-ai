package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ReservationType {
    BOOKING,
    HOLD,
    MAINTENANCE,
    OTHER
}
