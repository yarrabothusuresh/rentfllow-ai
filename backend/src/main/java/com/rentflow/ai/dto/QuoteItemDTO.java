package com.rentflow.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rentflow.ai.model.PricingStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuoteItemDTO {
    private UUID id;
    private UUID quoteId;
    private UUID productId;
    private String productName;
    private String productSku;
    private String description;
    private int quantity = 1;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal standardUnitPrice = BigDecimal.ZERO;
    private BigDecimal priceOverrideDifference = BigDecimal.ZERO;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private PricingStrategy pricingStrategy = PricingStrategy.PER_EVENT;

    private int rentalDays = 1;
    private BigDecimal lineSubtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal lineTotal = BigDecimal.ZERO;

    // Availability validation check feedback
    private boolean available = true;
    private int availableQuantity = 0;
    private int shortageQuantity = 0;

    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuoteItemDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getStandardUnitPrice() { return standardUnitPrice; }
    public void setStandardUnitPrice(BigDecimal standardUnitPrice) { this.standardUnitPrice = standardUnitPrice; }

    public BigDecimal getPriceOverrideDifference() { return priceOverrideDifference; }
    public void setPriceOverrideDifference(BigDecimal priceOverrideDifference) { this.priceOverrideDifference = priceOverrideDifference; }

    public PricingStrategy getPricingStrategy() { return pricingStrategy; }
    public void setPricingStrategy(PricingStrategy pricingStrategy) { this.pricingStrategy = pricingStrategy; }

    public int getRentalDays() { return rentalDays; }
    public void setRentalDays(int rentalDays) { this.rentalDays = rentalDays; }

    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public void setLineSubtotal(BigDecimal lineSubtotal) { this.lineSubtotal = lineSubtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public int getShortageQuantity() { return shortageQuantity; }
    public void setShortageQuantity(int shortageQuantity) { this.shortageQuantity = shortageQuantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
