# Ideal Customer Profile (ICP) Hypothesis

> **Status: Working ICP Hypothesis — To be validated with real customers.**

This document outlines the initial assumptions and product design guidelines for RentFlow AI's primary target market. It serves as our solution-fit blueprint and has not yet been verified against statistically representative market data.

---

## 1. Primary Target Customer
US-based event and party rental companies operating a high-volume, asset-heavy logistics model. 

* **Example Target Profile**: Evergreen Event Rentals (Dallas, Texas).
* **Business Model**: B2B/B2C event supply chain (renting party goods, tents, AV equipment, and tables).

---

## 2. Business Profile Parameters (Hypothesis)

| Parameter | Value / Range |
| :--- | :--- |
| **Annual Revenue** | $500K – $10M |
| **Employees** | 10 – 50 full-time staff |
| **Locations/Warehouses** | 1 – 3 regional hubs |
| **Unique Rental Products** | 500 – 10,000+ items |
| **Events Managed** | 50 – 500+ per month |
| **Geographic Market** | United States |
| **Client Types** | Wedding planners, corporate event coordinators, venues, nonprofits, hotels, restaurants, and individuals. |

---

## 3. Key Personas

### Owner (e.g., Mike)
* **Aspiration**: Scale business without linear headcount growth.
* **Goals**: Increase revenue & profit margins, reduce operational errors, get real-time dashboards of utilization and cash flow.
* **Metric**: Net Profit Margin, Asset Return on Investment.

### Sales Manager (e.g., Sarah)
* **Aspiration**: Close deals faster and eliminate forgotten leads.
* **Goals**: Instantly qualify inquiries, draft and send quotes under 10 minutes, automate follow-ups for outstanding proposals.
* **Metric**: Quote Conversion Rate, Average Lead-to-Quote time.

### Warehouse Manager (e.g., James)
* **Aspiration**: Zero logistics errors and full accountability.
* **Goals**: Automatically generate pick/load lists from confirmed bookings, ensure items are prepped, and log returns/inspections.
* **Metric**: Packing Error Rate, Shortage Incidents.

### Operations Manager (e.g., Alex)
* **Aspiration**: Fully optimized dispatch scheduling and driver routes.
* **Goals**: Track deliveries and setups, prevent scheduling conflicts, optimize truck cargo packing.
* **Metric**: On-time Delivery Rate, Fuel & Vehicle Efficiency.

---

## 4. Pain Points & Product Capabilities Mapping

| Pain Point | RentFlow AI Capability | Business Value |
| :--- | :--- | :--- |
| **1. Too many scattered inquiries** | AI Central Lead Router | Automatically qualifies and aggregates leads from email, SMS, and web forms. |
| **2. Time-consuming manual quoting** | AI Quote Generator | Automatically drafts margin-optimized proposals in under 10 minutes. |
| **3. Inventory double-booking** | AI Availability Intelligence | Real-time date-specific checks block quotes for items already committed. |
| **4. Unanswered quotes/cold leads** | Automated AI Sales Follow-ups | Sends polite reminders at optimal times to recover idle deals. |
| **5. Operational silo (Sales to Logistcs)** | Connected Rental Workflow | Direct state change triggers pick-lists and dispatch calendars. |
| **6. Unknown event profit margins** | AI Margin Estimator | Calculates expected margin by factor-indexing delivery, labor, and item cost. |
| **7. Slow deposit collections** | Automated Deposit Workflow | Sends digital payment/pre-auth links immediately upon quote signing. |
| **8. Unclear utilization metrics** | AI Business Copilot | Resolves natural language business queries on inventory performance. |

---

## 5. Qualification Questionnaire Scoring Model

To evaluate fit in a B2B demo context, we use a simple additive score:
1. **Catalog Size**: 5,000+ items (25 pts) \| 500 - 5000 (25 pts) \| 100 - 500 (15 pts) \| < 100 (10 pts)
2. **Monthly Events**: 50+ events (25 pts) \| 20 - 50 (15 pts) \| < 20 (10 pts)
3. **Employees**: 11+ employees (25 pts) \| 6 - 10 (15 pts) \| 1 - 5 (10 pts)
4. **Current Quote Tools**: Spreadsheet/Manual (25 pts) \| Rental Software (20 pts) \| Custom (15 pts)

### Score Categories
* **0 – 30**: Early-stage rental business (primarily localized, small catalog).
* **31 – 60**: Growing rental business (scaling operations, fits basic workflows).
* **61 – 80**: Strong RentFlow AI candidate (experiencing friction points, high ROI potential).
* **81 – 100**: Ideal RentFlow AI customer (fits the target profile of high catalog complexity and high event volume).

---

## 6. What Needs Customer Validation

The following elements of our hypothesis require interviews and analytics to validate:
* **The $500K Revenue Floor**: Do companies below $500K have enough complexity to justify AI overhead, or is their constraint purely lead-volume related?
* **Average Time Saved**: Does the AI Quote Generator actually reduce average quote creation times by 80%?
* **Personnel Structures**: Do all target organizations have discrete Warehouse vs Operations roles, or are they merged?
