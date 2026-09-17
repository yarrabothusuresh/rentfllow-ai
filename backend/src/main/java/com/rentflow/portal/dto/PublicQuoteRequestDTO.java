package com.rentflow.portal.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PublicQuoteRequestDTO {

    public static class QuoteItemRequestDTO {
        private UUID productId;
        private int quantity;

        public QuoteItemRequestDTO() {}

        public QuoteItemRequestDTO(UUID productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    private LocalDate startDate;
    private LocalDate endDate;
    private String eventName;
    private String eventType;
    private UUID deliveryAddressId;
    private String deliveryAddressText;
    private List<QuoteItemRequestDTO> items = new ArrayList<>();
    private String notes;

    // Guest details if not logged in
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String guestCompany;

    private String idempotencyKey;

    public PublicQuoteRequestDTO() {}

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getDeliveryAddressId() { return deliveryAddressId; }
    public void setDeliveryAddressId(UUID deliveryAddressId) { this.deliveryAddressId = deliveryAddressId; }

    public String getDeliveryAddressText() { return deliveryAddressText; }
    public void setDeliveryAddressText(String deliveryAddressText) { this.deliveryAddressText = deliveryAddressText; }

    public List<QuoteItemRequestDTO> getItems() { return items; }
    public void setItems(List<QuoteItemRequestDTO> items) { this.items = items; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestCompany() { return guestCompany; }
    public void setGuestCompany(String guestCompany) { this.guestCompany = guestCompany; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
