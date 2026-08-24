package com.rentflow.warehouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum WarehouseOrderStatus {
    PENDING,
    READY_TO_PICK,
    PICKING,
    PICKED,
    PACKING,
    PACKED,
    READY_FOR_DELIVERY,
    CANCELLED
}
