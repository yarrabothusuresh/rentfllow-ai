package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PricingStrategy {
    PER_EVENT,
    PER_DAY,
    PER_WEEK,
    FLAT_RATE;

    @JsonCreator
    public static PricingStrategy fromValue(Object value) {
        if (value == null) return PER_EVENT;
        String str = value.toString();
        for (PricingStrategy s : values()) {
            if (s.name().equalsIgnoreCase(str)) {
                return s;
            }
        }
        return PER_EVENT;
    }
}
