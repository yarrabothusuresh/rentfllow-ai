package com.rentflow.ai.service;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.DiscountType;
import com.rentflow.ai.model.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class QuoteCalculationService {

    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public QuoteCalculationResponse calculate(QuoteCalculationRequest request) {
        QuoteCalculationResponse resp = new QuoteCalculationResponse();

        BigDecimal grossSubtotal = BigDecimal.ZERO;

        // 1. Calculate each line item
        if (request.getItems() != null) {
            for (QuoteItemDTO item : request.getItems()) {
                BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                int qty = Math.max(0, item.getQuantity());
                int days = Math.max(1, item.getRentalDays());
                PricingStrategy strategy = item.getPricingStrategy() != null ? item.getPricingStrategy() : PricingStrategy.PER_EVENT;

                BigDecimal multiplier = BigDecimal.ONE;
                if (strategy == PricingStrategy.PER_DAY) {
                    multiplier = BigDecimal.valueOf(days);
                } else if (strategy == PricingStrategy.PER_WEEK) {
                    int weeks = (int) Math.ceil((double) days / 7.0);
                    multiplier = BigDecimal.valueOf(Math.max(1, weeks));
                }

                BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(qty))
                        .multiply(multiplier)
                        .setScale(2, ROUNDING);

                item.setLineSubtotal(lineSubtotal);

                // Standard vs Override comparison tracking
                if (item.getStandardUnitPrice() != null && item.getStandardUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = unitPrice.subtract(item.getStandardUnitPrice()).setScale(2, ROUNDING);
                    item.setPriceOverrideDifference(diff);
                } else {
                    item.setStandardUnitPrice(unitPrice);
                    item.setPriceOverrideDifference(BigDecimal.ZERO);
                }

                grossSubtotal = grossSubtotal.add(lineSubtotal).setScale(2, ROUNDING);
                resp.getCalculatedItems().add(item);
            }
        }

        resp.setSubtotal(grossSubtotal);

        // 2. Calculate Order Discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discVal = request.getDiscountValue() != null ? request.getDiscountValue() : BigDecimal.ZERO;
        DiscountType discType = request.getDiscountType() != null ? request.getDiscountType() : DiscountType.PERCENTAGE;

        if (discVal.compareTo(BigDecimal.ZERO) > 0 && grossSubtotal.compareTo(BigDecimal.ZERO) > 0) {
            if (discType == DiscountType.PERCENTAGE) {
                discountAmount = grossSubtotal.multiply(discVal)
                        .divide(new BigDecimal("100"), 2, ROUNDING);
            } else {
                discountAmount = discVal.setScale(2, ROUNDING);
            }
            // Discount cannot exceed gross subtotal
            if (discountAmount.compareTo(grossSubtotal) > 0) {
                discountAmount = grossSubtotal;
            }
        }
        resp.setDiscountAmount(discountAmount);

        // 3. Sum Order Fees
        BigDecimal delivery = request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal pickup = request.getPickupFee() != null ? request.getPickupFee() : BigDecimal.ZERO;
        BigDecimal setup = request.getSetupFee() != null ? request.getSetupFee() : BigDecimal.ZERO;
        BigDecimal breakdown = request.getBreakdownFee() != null ? request.getBreakdownFee() : BigDecimal.ZERO;
        BigDecimal service = request.getServiceFee() != null ? request.getServiceFee() : BigDecimal.ZERO;

        BigDecimal totalFees = delivery.add(pickup).add(setup).add(breakdown).add(service).setScale(2, ROUNDING);
        resp.setDeliveryFee(delivery.setScale(2, ROUNDING));
        resp.setPickupFee(pickup.setScale(2, ROUNDING));
        resp.setSetupFee(setup.setScale(2, ROUNDING));
        resp.setBreakdownFee(breakdown.setScale(2, ROUNDING));
        resp.setServiceFee(service.setScale(2, ROUNDING));
        resp.setTotalFees(totalFees);

        // 4. Calculate Tax
        BigDecimal discountedSubtotal = grossSubtotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, ROUNDING);
        BigDecimal taxableAmount = discountedSubtotal.add(totalFees).setScale(2, ROUNDING);
        resp.setTaxableAmount(taxableAmount);

        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : new BigDecimal("8.25");
        resp.setTaxRate(taxRate.setScale(2, ROUNDING));

        BigDecimal taxAmount = taxableAmount.multiply(taxRate)
                .divide(new BigDecimal("100"), 2, ROUNDING);
        resp.setTaxAmount(taxAmount);

        // 5. Total Order Amount
        BigDecimal totalAmount = taxableAmount.add(taxAmount).setScale(2, ROUNDING);
        resp.setTotalAmount(totalAmount);

        // 6. Deposit & Balance
        BigDecimal depositPct = request.getDepositPercentage() != null ? request.getDepositPercentage() : new BigDecimal("30.00");
        resp.setDepositPercentage(depositPct.setScale(2, ROUNDING));

        BigDecimal depositAmount = totalAmount.multiply(depositPct)
                .divide(new BigDecimal("100"), 2, ROUNDING);
        resp.setDepositAmount(depositAmount);

        BigDecimal remainingBalance = totalAmount.subtract(depositAmount).max(BigDecimal.ZERO).setScale(2, ROUNDING);
        resp.setRemainingBalance(remainingBalance);

        return resp;
    }
}
