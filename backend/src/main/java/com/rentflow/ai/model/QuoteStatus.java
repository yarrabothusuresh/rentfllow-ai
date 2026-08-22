package com.rentflow.ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum QuoteStatus {
    DRAFT,
    PENDING_REVIEW,
    SENT,
    VIEWED,
    ACCEPTED,
    CHANGE_REQUESTED,
    DECLINED,
    REJECTED,
    EXPIRED,
    CANCELLED;

    @JsonCreator
    public static QuoteStatus fromValue(Object value) {
        if (value == null) return DRAFT;
        String str = value.toString();
        for (QuoteStatus s : values()) {
            if (s.name().equalsIgnoreCase(str)) {
                return s;
            }
        }
        return DRAFT;
    }
}
