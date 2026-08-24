package com.rentflow.ai.dto;

import com.rentflow.ai.model.ReservationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BatchReservationRequestDTO {

    private UUID bookingId;
    private UUID eventId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ReservationType reservationType = ReservationType.BOOKING;
    private List<ItemRequest> items;

    public static class ItemRequest {
        private UUID productId;
        private int quantity;

        public ItemRequest() {}

        public ItemRequest(UUID productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public BatchReservationRequestDTO() {}

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public ReservationType getReservationType() { return reservationType; }
    public void setReservationType(ReservationType reservationType) { this.reservationType = reservationType; }

    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }
}
