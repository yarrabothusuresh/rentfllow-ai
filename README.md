# RentFlow AI — Enterprise Rental Management Operating System

> **"Turn every rental inquiry into a profitable, executed booking."**

RentFlow AI is an AI-native, multi-tenant rental operating system purpose-built for US party, event, and heavy equipment rental companies. It unifies customer discovery, real-time availability, dynamic pricing, quotes, contracts, payments, warehouse fulfillment, dispatch logistics, return inspections, damage claims, autonomous AI sales, internal copilot, proactive recommendations, and voice/phone AI.

---

## Architecture & System Overview

```
                          ┌─────────────────────────────────────────────────────────┐
                          │                   CLIENT EXPERIENCES                    │
                          │   Storefront  •  Customer Portal  •  Internal Dashboard  │
                          └───────────────────────────┬─────────────────────────────┘
                                                      │ (HTTPS / REST / WebSocket)
                                                      ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                     SPRING BOOT BACKEND CORE                                      │
├───────────────────────────┬───────────────────────────┬───────────────────────────────────────────┤
│    COMMERCE & CATALOG     │  OPERATIONS & LOGISTICS   │               FINANCE & CRM               │
│ • Products & Categories   │ • Availability Engine     │ • CRM Leads & Inquiries                   │
│ • Kits & Bundles          │ • Warehouse Pick & Pack   │ • Dynamic Quotes & Tax Calculations       │
│ • Inventory Tracking      │ • Route Dispatch & Fleet  │ • Contracts & Digital Signatures          │
│ • Multi-Location Support  │ • Returns & Inspection    │ • Stripe Payments, Invoices & Balances    │
│ • Damage Claims & Repair  │ • Drivers & Vehicles      │ • Margin & Profitability Analytics        │
├───────────────────────────┴───────────────────────────┴───────────────────────────────────────────┤
│                                   INTELLIGENCE & AUTOMATION                                       │
│ • AI Sales Agent (Customer discovery, lead capture, draft quote generation)                       │
│ • Internal AI Copilot (Natural language business Q&A, operational search, explainable insights)   │
│ • AI Recommendations & Automation (Signal detection, rule evaluation, safe execution)             │
│ • Phone AI Foundation (Inbound telephony, voice sales, recording consent, human handoff)          │
├───────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                     ENTERPRISE HARDENING                                          │
│ • Multi-Tenant Data Isolation  • Token Bucket Rate Limiting  • SSRF Protection                    │
│ • Strict CSP & Security Headers • PostgreSQL Multi-Stage Docker  • Zero-Leak Health Probes        │
└───────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 30-Day Platform Roadmap Completion

| Phase | Milestone | Features Delivered |
| :--- | :--- | :--- |
| **Days 1–5** | **Platform Core & Commerce** | Multi-tenant auth, user roles, product catalog, categories, pricing models. |
| **Days 6–10** | **Inventory & Availability** | Serialized items, real-time date availability, conflict detection, buffer days. |
| **Days 11–15** | **Order-to-Cash Workflow** | Quotes, tax calculation, e-signatures, deposit payments, confirmed bookings. |
| **Days 16–20** | **Logistics & Fleet** | Warehouse orders, pick lists, load lists, delivery routes, vehicle assignments. |
| **Days 21–25** | **Returns & Customer Portal** | Check-in inspection, damage claims, repair estimates, self-service customer portal. |
| **Day 26** | **CRM & Analytics** | Lead stages, conversion tracking, sales follow-ups, margin profitability analytics. |
| **Day 27** | **AI Sales Agent** | Conversational storefront agent, availability discovery, lead and quote capture. |
| **Day 28** | **Internal AI Copilot** | Natural language executive assistant, explainable analytics, safe tool invocation. |
| **Day 29** | **Proactive Automation** | Real-time business signal detectors, rule-based recommendations, human confirmation. |
| **Day 30** | **Production Readiness & Phone AI** | Phone AI foundation, security hardening (CSP, SSRF, Rate Limiting), master E2E scenario. |

---

## Quick Start (Local Development)

### 1. Backend (Spring Boot 3.2 + Java 17)
```bash
cd backend
mvn spring-boot:run
# Backend will start on http://localhost:8080
```

### 2. Frontend (Angular 17 + TypeScript + SCSS)
```bash
cd frontend
npm install
npm start
# Frontend will start on http://localhost:4200
```

---

## Production Deployment (Docker Compose)

RentFlow AI comes with a production-hardened multi-stage Docker build and Docker Compose configuration:

```bash
# Build and run PostgreSQL 15, backend, and frontend containers
docker compose up -d --build

# Verify container health
docker compose ps
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/ready
```

For Kubernetes, Nginx, and cloud deployment guides, see [DEPLOYMENT.md](DEPLOYMENT.md).

---

## Key Modules & Navigation

- **Executive Dashboard**: `/dashboard/home`
- **Storefront & Catalog**: `/storefront`
- **Quotes Management**: `/dashboard/quotes`
- **Bookings & Calendar**: `/dashboard/bookings`
- **Warehouse Fulfillment**: `/dashboard/warehouse`
- **Delivery Fleet**: `/dashboard/deliveries`
- **Returns & Inspection**: `/dashboard/returns`
- **Damage Claims**: `/dashboard/claims`
- **Invoices & Payments**: `/dashboard/invoices`
- **CRM & Pipeline**: `/dashboard/crm`
- **AI Copilot**: Floating modal or `Ctrl+K`
- **AI Recommendations**: `/dashboard/automation`
- **Phone AI Console**: `/dashboard/phone-ai`
- **Customer Portal**: `/portal/login`

---

## Security & Reliability Highlights
- **Tenant Isolation**: Non-bypassable `tenantId` filtering across every SQL query and service method. Verified by `Day30CrossTenantSecurityTest`.
- **SSRF Protection**: Outbound webhook validator blocks RFC 1918 private IPs and cloud metadata IP (169.254.169.254).
- **Phone AI Safety Guardrail**: AI is strictly prohibited from accepting credit card numbers or processing payments over voice.
- **Fail-Safe Startup**: `SecurityStartupValidator` blocks execution in production if default secrets or insecure CORS configurations are detected.

---

## Documentation Index
- [Production Readiness Report](docs/day-30-production-readiness.md)
- [Security Architecture & Policy](SECURITY.md)
- [Deployment Guide](DEPLOYMENT.md)
- [Operations & Runbook](OPERATIONS.md)
- [Production Checklist](docs/production-checklist.md)
- [Master Demo Script (15–20 Mins)](docs/demo-script.md)
- [Post-Day 30 Product Backlog](docs/post-30-day-backlog.md)

---

## License & Intellectual Property
© 2026 RentFlow AI. All rights reserved. Confidential & Proprietary.
