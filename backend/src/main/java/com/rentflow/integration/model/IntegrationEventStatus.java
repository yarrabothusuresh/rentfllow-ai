package com.rentflow.integration.model;

public enum IntegrationEventStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    PARTIAL,
    FAILED,
    DEAD_LETTER
}
