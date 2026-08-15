package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TransactionType {
    PURCHASE,
    ADJUSTMENT,
    RESERVATION,
    RELEASE,
    ALLOCATE,
    CHECKOUT,
    RETURN,
    DAMAGE,
    LOSS,
    MAINTENANCE,
    RESTORED
}
