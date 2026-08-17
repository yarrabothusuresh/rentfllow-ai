package com.rentflow.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rentflow.ai.model.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuoteDiscountDTO {
    private UUID id;
    private UUID quoteId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private DiscountType type = DiscountType.PERCENTAGE;

    private BigDecimal value = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;
    private String reason;
    private String approvedBy;
    private LocalDateTime createdAt;

    public QuoteDiscountDTO() {}

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
