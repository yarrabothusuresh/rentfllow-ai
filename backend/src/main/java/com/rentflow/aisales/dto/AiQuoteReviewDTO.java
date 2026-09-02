package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.MarginStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AiQuoteReviewDTO {
    private UUID quoteId;
    private String quoteNumber;
    private String status;
    private UUID conversationId;
    private String conversationPublicId;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String eventType;
    private String eventName;
    private LocalDate eventDate;
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;
    private String deliveryAddress;

    private List<AiQuoteReviewItemDTO> items = new ArrayList<>();

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal setupFee = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Internal Profitability Section (Staff only)
    private BigDecimal estimatedCost = BigDecimal.ZERO;
    private BigDecimal estimatedProfit = BigDecimal.ZERO;
    private double estimatedMarginPct = 0.0;
    private double targetMarginPct = 30.0;
    private MarginStatus marginStatus = MarginStatus.HEALTHY;
    private List<String> warnings = new ArrayList<>();
    private String aiRecommendationNotes;

    private boolean approved;
    private String approvedBy;
    private LocalDateTime approvedAt;

    public AiQuoteReviewDTO() {}

    public static class AiQuoteReviewItemDTO {
        private UUID productId;
        private String sku;
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private boolean available;
        private int availableQuantity;

        public AiQuoteReviewItemDTO() {}

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }

        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getConversationPublicId() { return conversationPublicId; }
    public void setConversationPublicId(String conversationPublicId) { this.conversationPublicId = conversationPublicId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalDateTime getRentalStart() { return rentalStart; }
    public void setRentalStart(LocalDateTime rentalStart) { this.rentalStart = rentalStart; }

    public LocalDateTime getRentalEnd() { return rentalEnd; }
    public void setRentalEnd(LocalDateTime rentalEnd) { this.rentalEnd = rentalEnd; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<AiQuoteReviewItemDTO> getItems() { return items; }
    public void setItems(List<AiQuoteReviewItemDTO> items) { this.items = items; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getSetupFee() { return setupFee; }
    public void setSetupFee(BigDecimal setupFee) { this.setupFee = setupFee; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public BigDecimal getEstimatedProfit() { return estimatedProfit; }
    public void setEstimatedProfit(BigDecimal estimatedProfit) { this.estimatedProfit = estimatedProfit; }

    public double getEstimatedMarginPct() { return estimatedMarginPct; }
    public void setEstimatedMarginPct(double estimatedMarginPct) { this.estimatedMarginPct = estimatedMarginPct; }

    public double getTargetMarginPct() { return targetMarginPct; }
    public void setTargetMarginPct(double targetMarginPct) { this.targetMarginPct = targetMarginPct; }

    public MarginStatus getMarginStatus() { return marginStatus; }
    public void setMarginStatus(MarginStatus marginStatus) { this.marginStatus = marginStatus; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public String getAiRecommendationNotes() { return aiRecommendationNotes; }
    public void setAiRecommendationNotes(String aiRecommendationNotes) { this.aiRecommendationNotes = aiRecommendationNotes; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
