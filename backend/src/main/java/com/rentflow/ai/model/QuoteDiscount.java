package com.rentflow.ai.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quote_discounts")
public class QuoteDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID quoteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type = DiscountType.PERCENTAGE;

    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    private String reason;
    private String approvedBy;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (value == null) value = BigDecimal.ZERO;
        if (amount == null) amount = BigDecimal.ZERO;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public DiscountType getType() { return type; }
    public void setType(DiscountType type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
