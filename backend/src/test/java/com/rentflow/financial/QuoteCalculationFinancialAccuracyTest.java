package com.rentflow.financial;

import com.rentflow.ai.dto.QuoteCalculationRequest;
import com.rentflow.ai.dto.QuoteCalculationResponse;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.DiscountType;
import com.rentflow.ai.model.PricingStrategy;
import com.rentflow.ai.service.QuoteCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuoteCalculationFinancialAccuracyTest {

    private QuoteCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new QuoteCalculationService();
    }

    @Test
    @DisplayName("Line items: PER_EVENT, PER_DAY, and PER_WEEK exact multiplication")
    void testPricingStrategies() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();

        QuoteItemDTO itemEvent = new QuoteItemDTO();
        itemEvent.setUnitPrice(new BigDecimal("150.00"));
        itemEvent.setQuantity(2);
        itemEvent.setPricingStrategy(PricingStrategy.PER_EVENT);

        QuoteItemDTO itemDay = new QuoteItemDTO();
        itemDay.setUnitPrice(new BigDecimal("25.50"));
        itemDay.setQuantity(4);
        itemDay.setRentalDays(3);
        itemDay.setPricingStrategy(PricingStrategy.PER_DAY); // 25.50 * 4 * 3 = 306.00

        QuoteItemDTO itemWeek = new QuoteItemDTO();
        itemWeek.setUnitPrice(new BigDecimal("80.00"));
        itemWeek.setQuantity(2);
        itemWeek.setRentalDays(10); // 10 days = 2 weeks -> 80.00 * 2 * 2 = 320.00
        itemWeek.setPricingStrategy(PricingStrategy.PER_WEEK);

        req.setItems(List.of(itemEvent, itemDay, itemWeek));
        req.setDiscountValue(BigDecimal.ZERO);
        req.setTaxRate(BigDecimal.ZERO);
        req.setDepositPercentage(BigDecimal.ZERO);

        QuoteCalculationResponse resp = calculationService.calculate(req);

        assertEquals(new BigDecimal("300.00"), itemEvent.getLineSubtotal());
        assertEquals(new BigDecimal("306.00"), itemDay.getLineSubtotal());
        assertEquals(new BigDecimal("320.00"), itemWeek.getLineSubtotal());
        assertEquals(new BigDecimal("926.00"), resp.getSubtotal());
        assertEquals(new BigDecimal("926.00"), resp.getTotalAmount());
    }

    @Test
    @DisplayName("Discount: percentage vs flat amount, and capping at subtotal")
    void testDiscountCalculations() {
        // 1. Percentage discount: 15% on $1000.00 -> $150.00
        QuoteCalculationRequest reqPct = new QuoteCalculationRequest();
        QuoteItemDTO item1 = new QuoteItemDTO();
        item1.setUnitPrice(new BigDecimal("500.00"));
        item1.setQuantity(2);
        reqPct.setItems(List.of(item1));
        reqPct.setDiscountType(DiscountType.PERCENTAGE);
        reqPct.setDiscountValue(new BigDecimal("15.00"));
        reqPct.setTaxRate(BigDecimal.ZERO);

        QuoteCalculationResponse respPct = calculationService.calculate(reqPct);
        assertEquals(new BigDecimal("1000.00"), respPct.getSubtotal());
        assertEquals(new BigDecimal("150.00"), respPct.getDiscountAmount());
        assertEquals(new BigDecimal("850.00"), respPct.getTotalAmount());

        // 2. Fixed amount discount: $250.00
        QuoteCalculationRequest reqFixed = new QuoteCalculationRequest();
        QuoteItemDTO item2 = new QuoteItemDTO();
        item2.setUnitPrice(new BigDecimal("1000.00"));
        item2.setQuantity(1);
        reqFixed.setItems(List.of(item2));
        reqFixed.setDiscountType(DiscountType.FIXED);
        reqFixed.setDiscountValue(new BigDecimal("250.00"));
        reqFixed.setTaxRate(BigDecimal.ZERO);

        QuoteCalculationResponse respFixed = calculationService.calculate(reqFixed);
        assertEquals(new BigDecimal("250.00"), respFixed.getDiscountAmount());
        assertEquals(new BigDecimal("750.00"), respFixed.getTotalAmount());

        // 3. Discount cap: discount cannot exceed gross subtotal
        QuoteCalculationRequest reqCapped = new QuoteCalculationRequest();
        QuoteItemDTO item3 = new QuoteItemDTO();
        item3.setUnitPrice(new BigDecimal("100.00"));
        item3.setQuantity(1);
        reqCapped.setItems(List.of(item3));
        reqCapped.setDiscountType(DiscountType.FIXED);
        reqCapped.setDiscountValue(new BigDecimal("500.00"));
        reqCapped.setTaxRate(BigDecimal.ZERO);

        QuoteCalculationResponse respCapped = calculationService.calculate(reqCapped);
        assertEquals(new BigDecimal("100.00"), respCapped.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), respCapped.getTotalAmount());
    }

    @Test
    @DisplayName("Compound fees and tax interactions with odd tax rate")
    void testFeesAndTaxInteraction() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        QuoteItemDTO item = new QuoteItemDTO();
        item.setUnitPrice(new BigDecimal("200.00"));
        item.setQuantity(5); // $1000.00
        req.setItems(List.of(item));

        // 10% discount -> $100.00 discount -> discounted subtotal = $900.00
        req.setDiscountType(DiscountType.PERCENTAGE);
        req.setDiscountValue(new BigDecimal("10.00"));

        // Fees: delivery $75.00, pickup $50.00, setup $120.00, breakdown $80.00, service $25.00 -> $350.00
        req.setDeliveryFee(new BigDecimal("75.00"));
        req.setPickupFee(new BigDecimal("50.00"));
        req.setSetupFee(new BigDecimal("120.00"));
        req.setBreakdownFee(new BigDecimal("80.00"));
        req.setServiceFee(new BigDecimal("25.00"));

        // Taxable = 900.00 + 350.00 = 1250.00
        // Tax rate = 8.25% -> 1250.00 * 0.0825 = 103.125 -> rounds HALF_UP to 103.13
        req.setTaxRate(new BigDecimal("8.25"));

        // Deposit = 25% of total (1250.00 + 103.13 = 1353.13)
        // 1353.13 * 0.25 = 338.2825 -> rounds HALF_UP to 338.28
        req.setDepositPercentage(new BigDecimal("25.00"));

        QuoteCalculationResponse resp = calculationService.calculate(req);

        assertEquals(new BigDecimal("1000.00"), resp.getSubtotal());
        assertEquals(new BigDecimal("100.00"), resp.getDiscountAmount());
        assertEquals(new BigDecimal("350.00"), resp.getTotalFees());
        assertEquals(new BigDecimal("1250.00"), resp.getTaxableAmount());
        assertEquals(new BigDecimal("8.25"), resp.getTaxRate());
        assertEquals(new BigDecimal("103.13"), resp.getTaxAmount());
        assertEquals(new BigDecimal("1353.13"), resp.getTotalAmount());
        assertEquals(new BigDecimal("25.00"), resp.getDepositPercentage());
        assertEquals(new BigDecimal("338.28"), resp.getDepositAmount());
        // Remaining balance = 1353.13 - 338.28 = 1014.85
        assertEquals(new BigDecimal("1014.85"), resp.getRemainingBalance());
    }

    @Test
    @DisplayName("Zero-value and edge cases: empty items, zero rates, zero balance")
    void testZeroEdgeCases() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        req.setItems(List.of());
        req.setTaxRate(BigDecimal.ZERO);
        req.setDepositPercentage(BigDecimal.ZERO);

        QuoteCalculationResponse resp = calculationService.calculate(req);

        assertEquals(new BigDecimal("0.00"), resp.getSubtotal());
        assertEquals(new BigDecimal("0.00"), resp.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), resp.getTotalFees());
        assertEquals(new BigDecimal("0.00"), resp.getTotalAmount());
        assertEquals(new BigDecimal("0.00"), resp.getDepositAmount());
        assertEquals(new BigDecimal("0.00"), resp.getRemainingBalance());
    }
}
