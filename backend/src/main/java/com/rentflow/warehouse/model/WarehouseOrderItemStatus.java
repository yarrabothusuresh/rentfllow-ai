package com.rentflow.warehouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum WarehouseOrderItemStatus {
    PENDING,
    PICKED,
    PARTIALLY_PICKED,
    PACKED,
    SHORT,
    DAMAGED
}
