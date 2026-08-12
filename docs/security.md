# Security Boundaries & Authorization Model

This document outlines the security architecture principles of **RentFlow AI**, emphasizing the separation between UI visibility and API protection.

---

## 1. Security Architecture Principles

In RentFlow AI, we follow a defense-in-depth model with a strict separation between:
1. **Frontend Authorization (UI Visibility Control)**
2. **Backend Authorization (API Protection & Data Isolation)**

```
               ┌────────────────────────┐
               │    Angular Frontend    │
               │                        │
               │   • Hides sidebar links│
               │   • Adjusts widgets    │
               │   • Swaps user views   │
               └───────────┬────────────┘
                           │
                           │ HTTP Request
                           ▼
               ┌────────────────────────┐
               │   Spring Boot API      │
               │                        │
               │   • Validates tenant   │
               │   • Checks permissions │
               │   • Enforces isolation │
               └────────────────────────┘
```

---

## 2. UI Authorization (Presentation Layer)
Frontend role checking is solely for user experience purposes:
* **Purpose**: Keep interfaces clean, hide irrelevant fields, and guide the user through their typical workflows.
* **Mechanism**: Angular structural directives (`*ngIf="hasPermission(...)"`) toggle links and render dashboard widgets.
* **Important Safety Rule**: The client-side application is executing on untrusted hardware. Users can manipulate JavaScript states, bypass route guards, or inspect Angular properties. **Client-side checks do NOT represent a security boundary.**

---

## 3. Backend Authorization (Security Boundary Layer)
All APIs and data queries must be secured on the server side:
* **Tenant Isolation**: Every database query must query data filtered by the client's authenticated `Tenant ID`.
* **API Permission Guards**: Server endpoints verify that the authenticated identity carries the required `PermissionCode` in their active role set (e.g. using Spring Security annotations like `@PreAuthorize("hasAuthority('PRODUCT_DELETE')")`).
* **Customer Isolation**: Customer-specific queries enforce a strict `userId = authenticatedUserId` constraint, ensuring one customer can never view another customer's quotes or payments.
* **User Management Constraints**: Admin accounts are restricted from altering Owner accounts in the user controller logic (e.g. checking target role codes on update requests).
