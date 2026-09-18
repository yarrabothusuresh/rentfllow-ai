package com.rentflow.ai.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class QuoteCalculationResponse {
    private static final java.math.RoundingMode ROUNDING = java.math.RoundingMode.HALF_UP;
    private BigDecimal subtotal = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal discountAmount = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal deliveryFee = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal pickupFee = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal setupFee = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal breakdownFee = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal serviceFee = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal totalFees = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal taxRate = new BigDecimal("8.25");
    private BigDecimal taxableAmount = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal taxAmount = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal depositPercentage = new BigDecimal("30.00");
    private BigDecimal depositAmount = BigDecimal.ZERO.setScale(2, ROUNDING);
    private BigDecimal remainingBalance = BigDecimal.ZERO.setScale(2, ROUNDING);
    private List<QuoteItemDTO> calculatedItems = new ArrayList<>();

    public QuoteCalculationResponse() {}

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

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

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositPercentage() { return depositPercentage; }
    public void setDepositPercentage(BigDecimal depositPercentage) { this.depositPercentage = depositPercentage; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public List<QuoteItemDTO> getCalculatedItems() { return calculatedItems; }
    public void setCalculatedItems(List<QuoteItemDTO> calculatedItems) { this.calculatedItems = calculatedItems; }
}
