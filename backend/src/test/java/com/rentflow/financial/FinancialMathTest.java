package com.rentflow.financial;

import com.rentflow.common.financial.FinancialMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class FinancialMathTest {

    @Test
    @DisplayName("Financial rounding rules: HALF_UP on .005 -> .01 and .004 -> .00")
    void testRoundingRules() {
        assertEquals(new BigDecimal("10.01"), FinancialMath.scaleCurrency(new BigDecimal("10.005")));
        assertEquals(new BigDecimal("10.00"), FinancialMath.scaleCurrency(new BigDecimal("10.004")));
        assertEquals(new BigDecimal("10.01"), FinancialMath.scaleCurrency(new BigDecimal("10.0051")));
        assertEquals(new BigDecimal("10.00"), FinancialMath.scaleCurrency(new BigDecimal("10.0049")));
        assertEquals(new BigDecimal("-10.01"), FinancialMath.scaleCurrency(new BigDecimal("-10.005")));
        assertEquals(new BigDecimal("-10.00"), FinancialMath.scaleCurrency(new BigDecimal("-10.004")));
    }

    @Test
    @DisplayName("Currency scaling: null safety and zero normalization")
    void testScaleCurrencyNullAndZero() {
        assertEquals(new BigDecimal("0.00"), FinancialMath.scaleCurrency(null));
        assertEquals(new BigDecimal("0.00"), FinancialMath.scaleCurrency(BigDecimal.ZERO));
        assertEquals(new BigDecimal("250.00"), FinancialMath.scaleCurrency(new BigDecimal("250")));
        assertEquals(new BigDecimal("99.90"), FinancialMath.scaleCurrency(new BigDecimal("99.9")));
    }

    @Test
    @DisplayName("Quantity multiplication: Exact decimal scaling")
    void testMultiplyQuantity() {
        assertEquals(new BigDecimal("49.95"), FinancialMath.multiply(new BigDecimal("9.99"), 5));
        assertEquals(new BigDecimal("0.00"), FinancialMath.multiply(new BigDecimal("9.99"), 0));
        assertEquals(new BigDecimal("0.00"), FinancialMath.multiply(null, 10));
        assertEquals(new BigDecimal("15000000.00"), FinancialMath.multiply(new BigDecimal("1500.00"), 10000));
    }

    @Test
    @DisplayName("Percentage amount calculations: taxes, discounts, deposits")
    void testCalculatePercentageAmount() {
        // 8.25% tax on $125.50 -> 125.50 * 8.25 / 100 = 10.35375 -> $10.35
        assertEquals(new BigDecimal("10.35"), FinancialMath.calculatePercentageAmount(new BigDecimal("125.50"), new BigDecimal("8.25")));

        // 15% discount on $200.00 -> $30.00
        assertEquals(new BigDecimal("30.00"), FinancialMath.calculatePercentageAmount(new BigDecimal("200.00"), new BigDecimal("15.00")));

        // 30% deposit on $6480.00 -> $1944.00
        assertEquals(new BigDecimal("1944.00"), FinancialMath.calculatePercentageAmount(new BigDecimal("6480.00"), new BigDecimal("30.00")));

        // Odd fractional rate: 7.125% on $333.33 -> 333.33 * 7.125 / 100 = 23.7497625 -> $23.75
        assertEquals(new BigDecimal("23.75"), FinancialMath.calculatePercentageAmount(new BigDecimal("333.33"), new BigDecimal("7.125")));

        // Null and zero handling
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculatePercentageAmount(null, new BigDecimal("10")));
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculatePercentageAmount(new BigDecimal("100"), null));
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculatePercentageAmount(BigDecimal.ZERO, new BigDecimal("10")));
    }

    @Test
    @DisplayName("Percentage division: Part over total with safe zero handling")
    void testCalculatePercentage() {
        // 25 out of 100 -> 25.00%
        assertEquals(new BigDecimal("25.00"), FinancialMath.calculatePercentage(new BigDecimal("25"), new BigDecimal("100")));

        // 1 out of 3 -> 33.33%
        assertEquals(new BigDecimal("33.33"), FinancialMath.calculatePercentage(new BigDecimal("1"), new BigDecimal("3")));

        // 2 out of 3 -> 66.67%
        assertEquals(new BigDecimal("66.67"), FinancialMath.calculatePercentage(new BigDecimal("2"), new BigDecimal("3")));

        // Safe zero denominator: must return 0.00 without ArithmeticException
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculatePercentage(new BigDecimal("50"), BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculatePercentage(null, new BigDecimal("100")));
    }

    @Test
    @DisplayName("Profit and Margin calculation: healthy, low, zero, and negative margins")
    void testMarginCalculations() {
        // Revenue 1000, Cost 400 -> Profit 600, Margin 60.00%
        BigDecimal rev1 = new BigDecimal("1000.00");
        BigDecimal cost1 = new BigDecimal("400.00");
        assertEquals(new BigDecimal("600.00"), FinancialMath.calculateProfit(rev1, cost1));
        assertEquals(new BigDecimal("60.00"), FinancialMath.calculateMargin(rev1, cost1));

        // Revenue 100, Cost 85 -> Profit 15, Margin 15.00%
        BigDecimal rev2 = new BigDecimal("100.00");
        BigDecimal cost2 = new BigDecimal("85.00");
        assertEquals(new BigDecimal("15.00"), FinancialMath.calculateProfit(rev2, cost2));
        assertEquals(new BigDecimal("15.00"), FinancialMath.calculateMargin(rev2, cost2));

        // Negative Margin: Revenue 100, Cost 150 -> Profit -50.00, Margin -50.00%
        BigDecimal rev3 = new BigDecimal("100.00");
        BigDecimal cost3 = new BigDecimal("150.00");
        assertEquals(new BigDecimal("-50.00"), FinancialMath.calculateProfit(rev3, cost3));
        assertEquals(new BigDecimal("-50.00"), FinancialMath.calculateMargin(rev3, cost3));

        // Zero Revenue: Revenue 0, Cost 100 -> Margin 0.00% (never division by zero)
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculateMargin(BigDecimal.ZERO, new BigDecimal("100.00")));
        assertEquals(new BigDecimal("0.00"), FinancialMath.calculateMargin(null, new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Large financial numbers: over $100M with exact precision")
    void testLargeFinancialAmounts() {
        BigDecimal largeRev = new BigDecimal("125450678.85");
        BigDecimal largeCost = new BigDecimal("83210450.40");

        BigDecimal profit = FinancialMath.calculateProfit(largeRev, largeCost);
        assertEquals(new BigDecimal("42240228.45"), profit);

        BigDecimal margin = FinancialMath.calculateMargin(largeRev, largeCost);
        // (42240228.45 * 100) / 125450678.85 = 33.670783... -> 33.67%
        assertEquals(new BigDecimal("33.67"), margin);
    }

    @Test
    @DisplayName("Safe add and subtract")
    void testSafeAddSubtract() {
        BigDecimal sum = FinancialMath.safeAdd(
                new BigDecimal("10.25"),
                null,
                new BigDecimal("5.75"),
                new BigDecimal("0.005") // rounds to .01 in final sum
        );
        assertEquals(new BigDecimal("16.01"), sum);

        BigDecimal diff = FinancialMath.safeSubtract(new BigDecimal("100.00"), new BigDecimal("35.50"));
        assertEquals(new BigDecimal("64.50"), diff);

        assertEquals(new BigDecimal("50.00"), FinancialMath.safeSubtract(new BigDecimal("50.00"), null));
        assertEquals(new BigDecimal("-50.00"), FinancialMath.safeSubtract(null, new BigDecimal("50.00")));
    }
}
