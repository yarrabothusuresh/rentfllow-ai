# RentFlow AI — Day 36: Financial Accuracy, BigDecimal Audit & AI Profitability Correction

## 1. Executive Summary & Core Financial Invariants
Financial correctness is a mission-critical P0 standard across RentFlow AI. Calculations regarding pricing, discounts, fees, sales tax, security deposits, payments, refunds, asset damage, replacement valuation, and profitability must adhere to strict mathematical invariants:
- **No Floating-Point Operations**: The primitive types `double` and `float` (and their boxed equivalents) are strictly prohibited for financial and currency calculations. All currency operations are represented using `java.math.BigDecimal`.
- **Standard Currency Scaling**: All monetary amounts are held and exported at a scale of 2 decimal places with `RoundingMode.HALF_UP`.
- **Rate and Percentage Scaling**: Intermediate rate multipliers use a scale of 4 (`RATE_SCALE = 4`), and final displayed percentages use a scale of 2 (`PERCENT_SCALE = 2`, `HALF_UP`).
- **Deterministic Rounding Utility**: All financial rounding and arithmetic are centralized in [`FinancialMath`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/common/financial/FinancialMath.java).
- **Authoritative AI Answers**: AI tools querying financial health or profitability query authoritative production services (`ProfitabilityService`, `BookingRepository`, `ProductRepository`) and never emit hardcoded demo values.

---

## 2. Centralized Financial Mathematics: `FinancialMath`
RentFlow AI standardizes financial math via [`com.rentflow.common.financial.FinancialMath`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/common/financial/FinancialMath.java):

```java
public final class FinancialMath {
    public static final int CURRENCY_SCALE = 2;
    public static final int RATE_SCALE = 4;
    public static final int PERCENT_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static BigDecimal scaleCurrency(BigDecimal amount);
    public static BigDecimal scalePercentage(BigDecimal percentage);
    public static BigDecimal multiply(BigDecimal unitPrice, int quantity);
    public static BigDecimal multiply(BigDecimal amount, BigDecimal multiplier);
    public static BigDecimal calculatePercentageAmount(BigDecimal base, BigDecimal percentage);
    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal total);
    public static BigDecimal calculateProfit(BigDecimal revenue, BigDecimal cost);
    public static BigDecimal calculateMargin(BigDecimal revenue, BigDecimal cost);
    public static BigDecimal safeAdd(BigDecimal... amounts);
    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b);
}
```

### 2.1 Rounding Rules
- Values with half-cents (e.g. `$10.005`) round up to `$10.01`.
- Values below half-cents (e.g. `$10.004`) round down to `$10.00`.
- Zero-denominator protection: In percentage and margin calculations, a zero or negative denominator safely returns `0.00%` without throwing `ArithmeticException`.

---

## 3. Financial Domain BigDecimal Audit

### 3.1 Quotes & Rental Price Calculations
In [`QuoteCalculationService`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/service/QuoteCalculationService.java):
- **Weekly Pricing Strategy**: Replaced floating-point division `Math.ceil((double) days / 7.0)` with exact integer ceil division `(days + 6) / 7`, completely removing double conversions.
- **Discount Computation**:
  - `PERCENTAGE`: Calculated as `(grossSubtotal * discountValue) / 100` with `HALF_UP`.
  - `FIXED_AMOUNT`: Capped strictly at `grossSubtotal` so an order can never produce a negative subtotal.
- **Tax Calculation**: Applied to `(discountedSubtotal + totalFees)` with standard tax rate scaling.
- **Deposit & Remaining Balance**: `depositAmount = (totalAmount * depositPct) / 100` with `remainingBalance = totalAmount - depositAmount`.

In [`BookingItem`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/model/BookingItem.java):
- Line subtotal is explicitly scaled at `@PrePersist` to 2 decimal places with `HALF_UP`.

In [`QuoteService`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/service/QuoteService.java):
- In `mapToDTO`, null-safe summation via `FinancialMath.safeAdd(...)` prevents `NullPointerException` when optional fee fields are null.

### 3.2 Storefront & Catalog Price Filters
In [`PublicCatalogController`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/portal/controller/PublicCatalogController.java) and [`PublicCatalogService`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/portal/service/PublicCatalogService.java):
- Query parameters `minPrice` and `maxPrice` were refactored from `Double` to `BigDecimal`, eliminating double-precision float parsing artifacts.

### 3.3 Damage Claims, Estimates & Billing
In [`DamageClaimService`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/claims/service/DamageClaimService.java) and [`ClaimBillingService`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/claims/service/ClaimBillingService.java):
- Replaced `BigDecimal.valueOf(25.00)` and `BigDecimal.valueOf(150.00)` with exact `new BigDecimal("25.00")` and `new BigDecimal("150.00")`.
- All estimate line item totals, claim subtotals, invoice adjustments, and balance calculations strictly enforce `.setScale(2, RoundingMode.HALF_UP)`.

---

## 4. Payment & Refund Integrity Architecture

### 4.1 Payment Accumulation & Overpayment Prevention
Payments accumulate dynamically inside `@Transactional` boundaries:
$$\text{Outstanding Balance} = \text{Total Amount} - \sum (\text{COMPLETED Payments})$$
Overpayment attempts beyond the outstanding balance fail immediately with `IllegalArgumentException`.

### 4.2 Refund Processing Engine
[`PaymentService.refundPayment`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/payment/service/PaymentService.java) provides atomic, row-locked refund processing:
1. **RBAC Validation**: Only authorized roles (`OWNER`, `ADMIN`, `FINANCE`) may process refunds.
2. **Payment State Validation**: Only `COMPLETED` payments can be refunded; voided or pending payments are rejected.
3. **Amount Validation**: Refund amount must be $> 0$ and $\le$ original payment amount.
4. **Full Refund**: Status transitions to `REFUNDED`.
5. **Partial Refund**: The payment amount is decremented by the refund amount, retaining `COMPLETED` status for the remaining amount.
6. **Multi-Entity Synchronization**:
   - `Booking` row is locked first (`PESSIMISTIC_WRITE`).
   - `Booking.depositPaid` is decremented.
   - `Booking.balanceDue` is incremented.
   - `Booking.status` transitions dynamically (`PAID` $\to$ `PARTIALLY_PAID` $\to$ `DEPOSIT_PENDING`).
   - Associated `Invoice` is synchronized (`amountPaid` and `balanceDue`).
   - Audit entry `PAYMENT_REFUNDED` is appended to `PaymentAudit`.

```
[Client POST /api/payments/{id}/refund]
                 |
                 v
   +-------------------------------+
   | RBAC & Status Pre-Check       |
   +---------------+---------------+
                   |
                   v
   +-------------------------------+
   | Acquire Row Lock: Booking     |
   +---------------+---------------+
                   |
                   v
   +-------------------------------+
   | Update Payment Status/Amount  |
   | (REFUNDED or Partial Deduct)  |
   +---------------+---------------+
                   |
                   v
   +-------------------------------+
   | Recalculate Booking Balances  |
   | depositPaid & balanceDue      |
   +---------------+---------------+
                   |
                   v
   +-------------------------------+
   | Sync Invoice Balances         |
   | amountPaid & balanceDue       |
   +---------------+---------------+
                   |
                   v
   +-------------------------------+
   | Write PaymentAudit Log        |
   +-------------------------------+
```

---

## 5. AI Profitability Engine Alignment

### 5.1 Authoritative Service Integration
Previous implementations of [`CalculateProfitabilityTool`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/tool/CalculateProfitabilityTool.java) used hardcoded values ($6,480 revenue, $2,920 cost, $3,560 profit, 54.9% margin) with `isDemoData: true`.

This has been updated to query authoritative entities:
- Looks up the `Booking` via UUID or booking number.
- Queries [`ProfitabilityService.getBookingProfitDetail`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/analytics/service/ProfitabilityService.java) for live revenue, asset amortization costs, delivery/pickup logistics costs, labor staging costs, and attributable damage costs.
- Returns live `BigDecimal` fields:
  - `revenue`
  - `estimatedCost`
  - `estimatedProfit`
  - `estimatedMargin`
  - `marginFlag`
  - `isAuthoritative: true`

### 5.2 Internal Sales Copilot Profitability
[`CheckInternalProfitabilityTool`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/aisales/tool/CheckInternalProfitabilityTool.java):
- Eradicated all `double` primitives.
- Uses `new BigDecimal("0.15")` for equipment wear rate.
- Uses `FinancialMath.calculateMargin(totalRevenue, totalCost)` with zero-safe division.
- Accurately classifies `MarginStatus`:
  - `HEALTHY` ($\ge 30.00\%$)
  - `LOW_MARGIN` ($< 20.00\%$)
  - `LOSS_MAKING` ($< 0.00\%$)

---

## 6. Verification Suite
The Day 36 implementation is backed by dedicated regression suites:
- [`FinancialMathTest`](file:///c:/dev/rentflow-ai/backend/src/test/java/com/rentflow/financial/FinancialMathTest.java): Precision, half-up rounding, zero division safety, large numbers ($100M+).
- [`QuoteCalculationFinancialAccuracyTest`](file:///c:/dev/rentflow-ai/backend/src/test/java/com/rentflow/financial/QuoteCalculationFinancialAccuracyTest.java): Pricing strategies (Day, Week, Event), discount caps, fee sums, tax calculations with odd rates (8.25%, 7.125%).
- [`PaymentAndRefundIntegrityTest`](file:///c:/dev/rentflow-ai/backend/src/test/java/com/rentflow/financial/PaymentAndRefundIntegrityTest.java): Payment accumulation, overpayment prevention, full and partial refund balance restoration, validation traps.
- [`AiProfitabilityToolTest`](file:///c:/dev/rentflow-ai/backend/src/test/java/com/rentflow/financial/AiProfitabilityToolTest.java): Validates AI tools produce authoritative results matching the backend analytics engine without floating-point distortion.
