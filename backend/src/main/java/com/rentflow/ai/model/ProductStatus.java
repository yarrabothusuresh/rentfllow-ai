package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DRAFT,
    DISCONTINUED;

    @JsonCreator
    public static ProductStatus fromValue(Object value) {
        if (value == null) return ACTIVE;
        String str = value.toString();
        for (ProductStatus s : values()) {
            if (s.name().equalsIgnoreCase(str)) {
                return s;
            }
        }
        return ACTIVE;
    }
}
