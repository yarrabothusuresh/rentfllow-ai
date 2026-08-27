# Day 19: Damage Claims, Repair/Maintenance, Replacement Cost & Loss Management

## 1. Objective
To build a comprehensive Damage Claims, Maintenance/Repair, Replacement Cost, and Loss Management system for RentFlow AI SaaS.
This completes the post-rental equipment lifecycle:
$$\text{RETURN} \rightarrow \text{CHECK-IN} \rightarrow \text{INSPECTION} \rightarrow \text{DAMAGE / MISSING} \rightarrow \text{DAMAGE CLAIM} \rightarrow \text{ASSESSMENT} \rightarrow \text{REPAIR / REPLACE} \rightarrow \text{ESTIMATE} \rightarrow \text{CUSTOMER REVIEW} \rightarrow \text{APPROVAL / DISPUTE} \rightarrow \text{RESOLUTION} \rightarrow \text{INVENTORY RESTORED / REPLACED / WRITTEN OFF}$$

---

## 2. Architecture Findings & Integration
- **Day 18 Returns & Inspections**: Reuses `ReturnOrder`, `ReturnOrderItem`, `Inspection`, `DamageRecord`.
- **Inventory Control**: Reuses `InventoryService`, `Product`, `InventoryReservation`, `InventoryTransaction`.
- **Invoicing & Billing**: `ClaimBillingService` creates draft damage charges on `InvoiceService` without auto-capturing money.
- **Tenant Isolation & Security**: Enforces `@PreAuthorize` RBAC and strict multi-tenant filtering on all entities and endpoints.

---

## 3. Domain Model Architecture
```
DamageClaim (1) ─── (N) DamageClaimItem
     │
     ├─── (N) ClaimEstimate (Immutable Versions)
     ├─── (N) RepairOrder
     └─── (N) ReplacementOrder
```

### Claim Status Lifecycle
`OPEN` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `ESTIMATE_CREATED` $\rightarrow$ `CUSTOMER_REVIEW` $\rightarrow$ `APPROVED` $\rightarrow$ `REPAIR_IN_PROGRESS` $\rightarrow$ `RESOLVED` (or `REPLACEMENT_REQUIRED` $\rightarrow$ `RESOLVED`, or `DISPUTED`, `WAIVED`, `CANCELLED`).

---

## 4. RBAC Matrix
| Role | View Claims | Create Estimate | Approve / Send Claim | Start / Complete Repair | Waive Claim | Customer Review |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **OWNER / ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **OPERATIONS_MANAGER** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **WAREHOUSE_MANAGER** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **WAREHOUSE_OPERATOR** | ✅ (Assigned) | ❌ | ❌ | ✅ (Assigned) | ❌ | ❌ |
| **SALES / FINANCE** | Read-Only | ❌ | ❌ | ❌ | ❌ | ❌ |
| **CUSTOMER** | Own Claims | ❌ | ❌ | ❌ | ❌ | Approve / Dispute |

---

## 5. API Endpoints
- `POST /api/damage-claims/from-return/{returnId}`
- `GET /api/damage-claims`
- `GET /api/damage-claims/{id}`
- `POST /api/damage-claims/{id}/estimate`
- `POST /api/damage-claims/{id}/send-to-customer`
- `POST /api/damage-claims/{id}/customer-approve`
- `POST /api/damage-claims/{id}/customer-dispute`
- `POST /api/damage-claims/{id}/waive`
- `POST /api/damage-claims/{id}/start-repair`
- `POST /api/damage-claims/{id}/replacement-required`
- `POST /api/damage-claims/{id}/resolve`
- `GET /api/damage-claims/dashboard`
- `GET /api/damage-claims/{id}/timeline`
- `POST /api/repairs` | `GET /api/repairs` | `POST /api/repairs/{id}/start` | `POST /api/repairs/{id}/complete` | `POST /api/repairs/{id}/fail`
- `POST /api/replacements` | `GET /api/replacements` | `POST /api/replacements/{id}/order` | `POST /api/replacements/{id}/receive` | `POST /api/replacements/{id}/complete`
