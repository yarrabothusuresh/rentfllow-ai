package com.rentflow.common.financial;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standard financial mathematics and rounding rules for RentFlow AI.
 * 
 * Rules:
 * 1. Currency amounts must always have a scale of 2 with RoundingMode.HALF_UP.
 * 2. Rate and percentage calculations use a scale of 4 for intermediate calculations
 *    and scale of 2 for displayed percentages.
 * 3. Never use primitive double or float for financial amounts or calculations.
 * 4. Division must always specify an explicit scale and RoundingMode to prevent ArithmeticException.
 * 5. Safe handling for null values and zero denominators.
 */
public final class FinancialMath {

    public static final int CURRENCY_SCALE = 2;
    public static final int RATE_SCALE = 4;
    public static final int PERCENT_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private FinancialMath() {}

    /**
     * Ensures an amount is non-null and properly scaled to 2 decimal places with HALF_UP.
     */
    public static BigDecimal scaleCurrency(BigDecimal amount) {
        if (amount == null) {
            return ZERO;
        }
        return amount.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    /**
     * Scales a percentage to standard 2 decimal places with HALF_UP.
     */
    public static BigDecimal scalePercentage(BigDecimal percentage) {
        if (percentage == null) {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE, ROUNDING_MODE);
        }
        return percentage.setScale(PERCENT_SCALE, ROUNDING_MODE);
    }

    /**
     * Exact multiplication of unit price by integer quantity, scaled to currency.
     */
    public static BigDecimal multiply(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null || quantity <= 0) {
            return ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    /**
     * Multiplication of an amount by a multiplier (e.g. rental days/weeks), scaled to currency.
     */
    public static BigDecimal multiply(BigDecimal amount, BigDecimal multiplier) {
        if (amount == null || multiplier == null || amount.signum() == 0 || multiplier.signum() == 0) {
            return ZERO;
        }
        return amount.multiply(multiplier).setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates a percentage portion of a base amount: (base * percentage) / 100.
     * Useful for discounts, taxes, and deposits.
     */
    public static BigDecimal calculatePercentageAmount(BigDecimal base, BigDecimal percentage) {
        if (base == null || percentage == null || base.signum() == 0 || percentage.signum() == 0) {
            return ZERO;
        }
        return base.multiply(percentage)
                .divide(new BigDecimal("100"), CURRENCY_SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates what percentage 'part' is of 'total': (part * 100) / total.
     * Returns 0.00 if total is null or zero.
     */
    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.signum() == 0) {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE, ROUNDING_MODE);
        }
        return part.multiply(new BigDecimal("100"))
                .divide(total, PERCENT_SCALE, ROUNDING_MODE);
    }

    /**
     * Computes gross profit: revenue - cost.
     */
    public static BigDecimal calculateProfit(BigDecimal revenue, BigDecimal cost) {
        BigDecimal rev = scaleCurrency(revenue);
        BigDecimal cst = scaleCurrency(cost);
        return rev.subtract(cst).setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    /**
     * Computes gross margin percentage: ((revenue - cost) * 100) / revenue.
     * Returns 0.00% if revenue is zero or negative.
     */
    public static BigDecimal calculateMargin(BigDecimal revenue, BigDecimal cost) {
        BigDecimal rev = scaleCurrency(revenue);
        BigDecimal cst = scaleCurrency(cost);
        if (rev.signum() <= 0) {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE, ROUNDING_MODE);
        }
        BigDecimal profit = rev.subtract(cst);
        return profit.multiply(new BigDecimal("100"))
                .divide(rev, PERCENT_SCALE, ROUNDING_MODE);
    }

    /**
     * Null-safe addition of multiple amounts.
     */
    public static BigDecimal safeAdd(BigDecimal... amounts) {
        BigDecimal sum = BigDecimal.ZERO;
        if (amounts != null) {
            for (BigDecimal a : amounts) {
                if (a != null) {
                    sum = sum.add(a);
                }
            }
        }
        return sum.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    /**
     * Null-safe subtraction (a - b).
     */
    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        BigDecimal first = a != null ? a : BigDecimal.ZERO;
        BigDecimal second = b != null ? b : BigDecimal.ZERO;
        return first.subtract(second).setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }
}
