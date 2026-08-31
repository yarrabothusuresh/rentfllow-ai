package com.rentflow.warehouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum WarehouseOrderStatus {
    PENDING,
    READY_TO_PICK,
    PICKING,
    PICKED,
    VERIFYING,
    PACKING,
    PACKED,
    LOADING,
    LOADED,
    READY_FOR_DELIVERY,
    HANDED_TO_DRIVER,
    SHORT,
    BLOCKED,
    CANCELLED
}
