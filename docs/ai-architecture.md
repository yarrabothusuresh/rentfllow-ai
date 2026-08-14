# RentFlow AI — AI Architecture Specification

This document details the software architecture, design patterns, safety principles, multi-tenant isolation, and authorization model for **RentFlow AI**'s AI Copilot & Business Assistant engine.

---

## 1. Overall AI Copilot Flow

```text
User Question
      ↓
AI Copilot (Angular Frontend)
      ↓
POST /api/ai/chat
      ↓
AI Orchestrator (Intent Recognition & Tool Selection)
      ↓
AIToolSecurityService (Role Authorization & Tenant Isolation)
      ↓
AITool Implementations (searchCustomer, getBooking, checkAvailability, etc.)
      ↓
Spring Boot Business Services / Repositories
      ↓
Database
      ↓
Tool Result Execution Payload
      ↓
AIProvider (MockAIProvider → Replaceable by OpenAI / Anthropic / Gemini / Ollama)
      ↓
AI Response DTO (Message, Intent, Tools Used, Suggested Actions, Approval Status, Safe Trace)
      ↓
AI Copilot UI
```

---

## 2. Safety & Security Principles

1. **No Direct Database Access**:
   The AI model and orchestrator never access the database directly via SQL or ORM queries. All database access must pass through controlled `AITool` implementations that call standard Spring Boot service/repository methods.

2. **Multi-Tenant Isolation**:
   Every `ToolRequest` carries a mandatory `tenantId`. `AIToolSecurityService` verifies that queries are strictly scoped to the authenticated user's tenant. Cross-tenant querying (e.g. Tenant A requesting Tenant B data) is blocked.

3. **Role-Based Tool Authorization**:
   - **OWNER**: Full access to all business intelligence, financial, and operational tools.
   - **ADMIN**: Access to operational, product, and inventory tools (excluding company owner administration).
   - **SALES**: Access to customer, lead, product, availability, quote, and booking tools.
   - **WAREHOUSE**: Access to inventory, product, booking, and warehouse task tools.
   - **DRIVER**: Access to delivery, pickup, and assigned booking tools.
   - **CUSTOMER**: Access ONLY to own quote, own booking, own payment, and own event. Cross-customer inquiries return `PERMISSION_DENIED`.

4. **Action Approval Workflow**:
   Controlled business actions that modify business state or send communications (e.g. `sendPaymentReminder`, `createQuote`, `sendQuote`, `reserveInventory`) return `status: "ACTION_REQUIRES_APPROVAL"` and `requiresApproval: true`. They require explicit user click on **Approve Action** before execution.

5. **Safe Operational Traces**:
   Internal LLM chain-of-thought and system prompts are never exposed to the client. The backend provides safe, high-level operational trace steps (e.g. `✓ Understood request`, `✓ Validated role permissions`, `✓ Executed tool searchCustomer`).

6. **Pluggable AIProvider Pattern**:
   The backend decouples business tools and orchestration from the underlying AI model using the `AIProvider` interface. Switching from `MockAIProvider` to `OpenAIProvider`, `AnthropicProvider`, `GeminiProvider`, or `OllamaProvider` requires zero modification to business tools or security logic.
