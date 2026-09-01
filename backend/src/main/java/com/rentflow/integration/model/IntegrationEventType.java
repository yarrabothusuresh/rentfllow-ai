package com.rentflow.integration.model;

public enum IntegrationEventType {
    CUSTOMER_CREATED("customer.created"),
    CUSTOMER_UPDATED("customer.updated"),
    PRODUCT_CREATED("product.created"),
    PRODUCT_UPDATED("product.updated"),
    QUOTE_CREATED("quote.created"),
    QUOTE_SENT("quote.sent"),
    QUOTE_APPROVED("quote.approved"),
    BOOKING_CREATED("booking.created"),
    BOOKING_UPDATED("booking.updated"),
    BOOKING_CANCELLED("booking.cancelled"),
    INVOICE_CREATED("invoice.created"),
    INVOICE_UPDATED("invoice.updated"),
    INVOICE_SENT("invoice.sent"),
    PAYMENT_RECEIVED("payment.received"),
    PAYMENT_FAILED("payment.failed"),
    REFUND_COMPLETED("refund.completed"),
    INVENTORY_UPDATED("inventory.updated"),
    DELIVERY_COMPLETED("delivery.completed"),
    RETURN_COMPLETED("return.completed"),
    DAMAGE_CLAIM_CREATED("damage_claim.created"),
    WEBHOOK_TEST("webhook.test");

    private final String eventCode;

    IntegrationEventType(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventCode() {
        return eventCode;
    }

    public static IntegrationEventType fromEventCode(String code) {
        for (IntegrationEventType type : values()) {
            if (type.eventCode.equalsIgnoreCase(code) || type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
