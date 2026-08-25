package com.rentflow.delivery.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum DeliveryType {
    DELIVERY,
    PICKUP
}
