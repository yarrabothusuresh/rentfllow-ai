# RentFlow AI — Centralized Quote Calculation Service

The `QuoteCalculationService` provides a unified stateless calculation engine used by REST endpoints, Angular UI components, and AI Copilot tools.

## Key Calculation Flow

1. **Line Subtotal Resolution**: Each line item is computed with pricing strategy multiplier.
2. **Gross Subtotal Aggregation**: Sum of all line subtotals.
3. **Discount Deduction**: Supports Percentage (`%`) or Fixed (`$`) discounts.
4. **Logistics & Service Fees**: Aggregates Delivery, Pickup, Setup, Teardown, Service, and Other fees.
5. **Sales Tax Computation**: Applied to `(Subtotal - Discount) + Total Fees`. Default rate: 8.25% (Dallas, TX).
6. **Total Amount**: Grand total payable by customer.
7. **Deposit Required**: Computed as percentage of total amount (default 30%). Displayed separately from remaining balance.

## Verification & Precision Rules

- Money is NEVER stored or processed using `float` or `double`.
- `BigDecimal.setScale(2, RoundingMode.HALF_UP)` applied to all results.
- Sales role discount limit strictly capped at 20% max. Exceeding throws `IllegalArgumentException("Discount exceeds your permission limit.")`.
