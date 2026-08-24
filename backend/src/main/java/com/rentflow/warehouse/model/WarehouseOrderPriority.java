package com.rentflow.warehouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum WarehouseOrderPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
