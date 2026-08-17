# RentFlow AI — Pricing Engine & Multi-Strategy Rates

RentFlow AI supports flexible pricing strategies tailored to event and party rental operations.

## Pricing Strategies (`PricingStrategy`)

1. `PER_EVENT`: Single flat charge for the full event duration (default multiplier = 1).
2. `PER_DAY`: Daily rental rate calculated as `Quantity × Unit Price × Rental Days`.
3. `PER_WEEK`: Weekly rate calculated based on total rental weeks.
4. `FLAT_RATE`: Fixed package or service rate.

## Financial Calculations & Monetary Precision

All monetary calculations use `BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP` to ensure zero rounding drift.

```java
// Line Item Subtotal
BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).multiply(BigDecimal.valueOf(multiplier));

// Gross Subtotal
BigDecimal grossSubtotal = items.stream().map(QuoteItem::getLineSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

// Discount Application
BigDecimal discountedSubtotal = grossSubtotal.subtract(discountAmount);

// Logistics & Operational Fees
BigDecimal totalFees = deliveryFee.add(pickupFee).add(setupFee).add(breakdownFee).add(serviceFee);

// Tax Base & Tax Calculation
BigDecimal taxableAmount = discountedSubtotal.add(totalFees);
BigDecimal taxAmount = taxableAmount.multiply(taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));

// Final Total & Deposit Required
BigDecimal totalAmount = taxableAmount.add(taxAmount);
BigDecimal depositAmount = totalAmount.multiply(depositPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
BigDecimal remainingBalance = totalAmount.subtract(depositAmount);
```
