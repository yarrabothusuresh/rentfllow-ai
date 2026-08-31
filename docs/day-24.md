# Day 24 — Business Intelligence, Profitability & KPI Analytics

## 1. Objective
RentFlow AI Day 24 establishes the authoritative Business Intelligence (BI) layer across financial, inventory, operational, and customer lifecycle data. It computes exact revenues, gross profits, margins, inventory utilization rates, quote sales funnels, and accounts receivable aging without relying on client-side approximations.

---

## 2. Existing Data Sources & Financial Audit

| Entity | Revenue Fields | Cost Fields | Status & Lifecycle Attributes |
| :--- | :--- | :--- | :--- |
| **Booking** | `subtotal` (rental), `deliveryFee`, `pickupFee`, `setupFee`, `breakdownFee`, `serviceFee`, `discountAmount`, `taxAmount`, `totalAmount` | Direct product allocation cost, delivery vehicle expense, labor allocations, attributable damage/repair | `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| **Quote** | `subtotal`, `deliveryFee`, `setupFee`, `serviceFee`, `discountAmount`, `totalAmount` | Estimated direct inventory cost, estimated logistics | `DRAFT`, `SENT`, `APPROVED`, `DECLINED`, `EXPIRED` |
| **Invoice** | `subtotal`, `discount`, `fees`, `tax`, `totalAmount`, `amountPaid`, `balanceDue` | — | `DRAFT`, `ISSUED`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `VOID` |
| **Payment** | `amount` | — | `COMPLETED`, `PENDING`, `FAILED`, `REFUNDED` |
| **Product** | `rentalPrice` | `replacementCost`, default direct cost estimate (standard 25% allocation or replacement depreciation) | `ACTIVE`, `DISCONTINUED`, `ARCHIVED` |
| **DamageClaim** | — | `estimatedTotalCost`, `approvedTotalCost`, `finalTotalCost` | `OPEN`, `ASSESSED`, `APPROVED`, `DISPUTED`, `WAIVED`, `CLOSED` |
| **RepairOrder** | — | `estimatedCost`, `actualCost` | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| **Delivery** | `deliveryFee` (on booking) | Vehicle usage estimate / driver duration | `SCHEDULED`, `ASSIGNED`, `DISPATCHED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `FAILED` |
| **ReturnOrder** | `pickupFee` (on booking) | — | `SCHEDULED`, `IN_TRANSIT`, `RECEIVED`, `INSPECTED`, `CLOSED` |
| **WarehouseOrder** | — | — | `READY_TO_PICK`, `PICKING`, `PACKING`, `LOADING`, `HANDED_TO_DRIVER`, `COMPLETED` |

---

## 3. Analytics Architecture

```
                 OPERATIONAL DATA SOURCES
 (Bookings, Invoices, Payments, Inventory, Damage, Repairs, Deliveries)
                             │
                             ▼
            ┌───────────────────────────────────┐
            │   ANALYTICS & KPI SERVICE LAYER   │
            │  - RevenueAnalyticsService        │
            │  - ProfitabilityService           │
            │  - InventoryAnalyticsService      │
            │  - CustomerAnalyticsService       │
            │  - OperationsAnalyticsService     │
            │  - AnalyticsExportService (CSV)   │
            │  - BusinessInsightService (AI)    │
            └───────────────────────────────────┘
                             │
            ┌────────────────┴──────────────────┐
            ▼                                   ▼
 REST ENDPOINTS (/api/analytics/**)     REPORT EXPORTS (CSV)
            │
            ▼
 ANGULAR BI DASHBOARD & DRILL-DOWNS
```

---

## 4. Authoritative Metric Formulations

### A. Revenue Definitions
- **Booked Revenue**: Sum of `totalAmount` for qualifying bookings in `CONFIRMED`, `IN_PROGRESS`, or `COMPLETED` status.
- **Invoiced Revenue**: Sum of `totalAmount` across all non-void invoices issued in the selected date range.
- **Collected Revenue**: Sum of `amount` across all successful `COMPLETED` payment transactions.
- **Outstanding Revenue**: Total Invoiced Amount minus Total Amount Paid across unpaid or partially paid invoices.
- **Average Booking Value (ABV)**: `Booked Revenue / Count(Qualifying Bookings)`.

### B. Profitability & Margin Engine
- **Gross Profit**:
  $$\text{Gross Profit} = \text{Revenue} - (\text{Direct Inventory Cost} + \text{Delivery Cost} + \text{Pickup/Labor Cost} + \text{Repair/Damage Cost})$$
- **Gross Margin %**:
  $$\text{Gross Margin \%} = \frac{\text{Gross Profit}}{\text{Revenue}} \times 100$$
- **Margin Flags**:
  - `HIGH_MARGIN` ($\ge 50\%$)
  - `HEALTHY_MARGIN` ($30\% - 49.9\%$)
  - `LOW_MARGIN` ($0\% - 29.9\%$)
  - `NEGATIVE_MARGIN` ($< 0\%$)

### C. Inventory Utilization
- **Quantity Products**:
  $$\text{Utilization \%} = \frac{\text{Total Quantity Rented Days}}{\text{Rentable Inventory Units} \times \text{Period Days}} \times 100$$
- **Serialized Assets**:
  $$\text{Serialized Utilization \%} = \frac{\text{Days Status is OUT\_ON\_RENT}}{\text{Total Available Days}} \times 100$$
- **Underutilized Alert Threshold**: $< 25\%$ utilization.
- **High Demand / Conflict Threshold**: $> 85\%$ utilization with active availability conflict records.

### D. Sales Funnel & Quote Conversion
- **Quote-to-Booking Conversion**:
  $$\text{Conversion \%} = \frac{\text{Count(Approved Quotes)}}{\text{Count(Eligible Quotes: SENT + APPROVED + DECLINED + EXPIRED)}} \times 100$$
- **Sales Funnel Stages**: `INQUIRIES (100%) -> QUOTES -> APPROVED -> BOOKINGS -> COMPLETED`.

### E. Accounts Receivable (AR) Aging Buckets
- **Current**: Due in the future or today ($\ge 0$ days past due).
- **1–30 Days**: 1 to 30 days overdue.
- **31–60 Days**: 31 to 60 days overdue.
- **61–90 Days**: 61 to 90 days overdue.
- **90+ Days**: Over 90 days overdue.

---

## 5. Security, RBAC & Tenant Isolation
- **Role Isolation**:
  - `OWNER`, `ADMIN`: Full access to all revenue, margins, and operational KPIs.
  - `FINANCE`: Full revenue, profit, invoice, payment, and AR aging visibility.
  - `OPERATIONS_MANAGER`: Fleet utilization, warehouse, delivery, return, and damage KPIs.
  - `SALES`: Quote conversion, customer revenue, and booking volumes.
  - `WAREHOUSE_MANAGER`: Warehouse throughput, fulfillment durations, and damage rates.
  - `DRIVER`, `CUSTOMER`: Blocked (`403 FORBIDDEN`) from internal BI metrics.
- **Multi-Tenant Scoping**: All queries enforce `WHERE tenant_id = :tenantId`.
- **Currency & Timezone**: All financial metrics use tenant currency (ISO code) and timestamps respect tenant timezone offset.
