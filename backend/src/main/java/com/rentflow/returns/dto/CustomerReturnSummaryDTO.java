package com.rentflow.returns.dto;

import com.rentflow.returns.model.ReturnOrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CustomerReturnSummaryDTO {

    private UUID returnOrderId;
    private String returnNumber;
    private String bookingNumber;
    private String eventName;
    private ReturnOrderStatus status;
    private LocalDate returnDate;
    private LocalDateTime completedAt;

    private List<CustomerReturnItemSummary> items;

    public UUID getReturnOrderId() { return returnOrderId; }
    public void setReturnOrderId(UUID returnOrderId) { this.returnOrderId = returnOrderId; }

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public ReturnOrderStatus getStatus() { return status; }
    public void setStatus(ReturnOrderStatus status) { this.status = status; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<CustomerReturnItemSummary> getItems() { return items; }
    public void setItems(List<CustomerReturnItemSummary> items) { this.items = items; }

    public static class CustomerReturnItemSummary {
        private String productName;
        private int quantityExpected;
        private int quantityReturned;
        private int quantityMissing;

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public int getQuantityExpected() { return quantityExpected; }
        public void setQuantityExpected(int quantityExpected) { this.quantityExpected = quantityExpected; }

        public int getQuantityReturned() { return quantityReturned; }
        public void setQuantityReturned(int quantityReturned) { this.quantityReturned = quantityReturned; }

        public int getQuantityMissing() { return quantityMissing; }
        public void setQuantityMissing(int quantityMissing) { this.quantityMissing = quantityMissing; }
    }
}
