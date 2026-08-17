package com.rentflow.ai.dto;

import com.rentflow.ai.model.DiscountType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class QuoteCalculationRequest {
    private List<QuoteItemDTO> items = new ArrayList<>();
    private List<QuoteFeeDTO> fees = new ArrayList<>();
    private DiscountType discountType = DiscountType.PERCENTAGE;
    private BigDecimal discountValue = BigDecimal.ZERO;
    private BigDecimal taxRate = new BigDecimal("8.25");
    private BigDecimal depositPercentage = new BigDecimal("30.00");
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal pickupFee = BigDecimal.ZERO;
    private BigDecimal setupFee = BigDecimal.ZERO;
    private BigDecimal breakdownFee = BigDecimal.ZERO;
    private BigDecimal serviceFee = BigDecimal.ZERO;

    public QuoteCalculationRequest() {}

    public List<QuoteItemDTO> getItems() { return items; }
    public void setItems(List<QuoteItemDTO> items) { this.items = items; }

    public List<QuoteFeeDTO> getFees() { return fees; }
    public void setFees(List<QuoteFeeDTO> fees) { this.fees = fees; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getDepositPercentage() { return depositPercentage; }
    public void setDepositPercentage(BigDecimal depositPercentage) { this.depositPercentage = depositPercentage; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getPickupFee() { return pickupFee; }
    public void setPickupFee(BigDecimal pickupFee) { this.pickupFee = pickupFee; }

    public BigDecimal getSetupFee() { return setupFee; }
    public void setSetupFee(BigDecimal setupFee) { this.setupFee = setupFee; }

    public BigDecimal getBreakdownFee() { return breakdownFee; }
    public void setBreakdownFee(BigDecimal breakdownFee) { this.breakdownFee = breakdownFee; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
}
