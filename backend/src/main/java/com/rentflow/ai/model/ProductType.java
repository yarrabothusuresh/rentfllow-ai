package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ProductType {
    RENTAL_ITEM,
    PACKAGE,
    SERVICE,
    CONSUMABLE;

    @JsonCreator
    public static ProductType fromValue(Object value) {
        if (value == null) return RENTAL_ITEM;
        String str = value.toString();
        for (ProductType t : values()) {
            if (t.name().equalsIgnoreCase(str)) {
                return t;
            }
        }
        return RENTAL_ITEM;
    }
}
