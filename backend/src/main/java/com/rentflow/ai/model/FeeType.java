package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum FeeType {
    DELIVERY,
    PICKUP,
    SETUP,
    BREAKDOWN,
    SERVICE,
    OTHER;

    @JsonCreator
    public static FeeType fromValue(Object value) {
        if (value == null) return OTHER;
        String str = value.toString();
        for (FeeType t : values()) {
            if (t.name().equalsIgnoreCase(str)) {
                return t;
            }
        }
        return OTHER;
    }
}
