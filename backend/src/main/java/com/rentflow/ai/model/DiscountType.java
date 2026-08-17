package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum DiscountType {
    PERCENTAGE,
    FIXED;

    @JsonCreator
    public static DiscountType fromValue(Object value) {
        if (value == null) return PERCENTAGE;
        String str = value.toString();
        for (DiscountType t : values()) {
            if (t.name().equalsIgnoreCase(str)) {
                return t;
            }
        }
        return PERCENTAGE;
    }
}
