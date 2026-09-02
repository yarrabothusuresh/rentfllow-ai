package com.rentflow.aisales.model;

public enum AiConversationStatus {
    ACTIVE,
    WAITING_FOR_CUSTOMER,
    WAITING_FOR_AGENT,
    WAITING_FOR_HUMAN,
    HUMAN_ACTIVE,
    QUOTE_DRAFTED,
    COMPLETED,
    CLOSED,
    ESCALATED
}
