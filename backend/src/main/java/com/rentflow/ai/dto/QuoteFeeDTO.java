package com.rentflow.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rentflow.ai.model.FeeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuoteFeeDTO {
    private UUID id;
    private UUID quoteId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private FeeType feeType = FeeType.OTHER;

    private String description;
    private BigDecimal amount = BigDecimal.ZERO;
    private LocalDateTime createdAt;

    public QuoteFeeDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public FeeType getFeeType() { return feeType; }
    public void setFeeType(FeeType feeType) { this.feeType = feeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
