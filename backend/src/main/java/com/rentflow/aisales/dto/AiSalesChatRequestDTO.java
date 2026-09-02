package com.rentflow.aisales.dto;

import java.util.UUID;

public class AiSalesChatRequestDTO {
    private String message;
    private UUID conversationId;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String channel; // INTERNAL, CUSTOMER_PORTAL, STOREFRONT, WEB_CHAT

    public AiSalesChatRequestDTO() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
