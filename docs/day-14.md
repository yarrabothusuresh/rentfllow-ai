# Day 14 — Notifications & Communications Module

## 1. Objective
Build a robust, extensible Notifications & Communications module supporting:
- Multi-channel delivery: In-App, Email (mock), and SMS (mock).
- Event-driven notifications via Spring `ApplicationEventPublisher` and `@EventListener`.
- Notification templates with safe variable rendering (`{{customerName}}`, `{{quoteNumber}}`, etc.).
- Customer and internal staff notification preferences.
- Delivery status tracking (`PENDING`, `PROCESSING`, `SENT`, `DELIVERED`, `FAILED`, `READ`).
- Retry management (up to 3 retries) and idempotency controls.
- Customer Portal Notification Center and Internal Staff Notification Center.
- Strict multi-tenant isolation and object-level authorization (`resource.customerId == user.customerId`).
- Audit logging (`NOTIFICATION_CREATED`, `NOTIFICATION_SENT`, `NOTIFICATION_FAILED`, `NOTIFICATION_READ`, `TEMPLATE_CREATED`, `PREFERENCE_UPDATED`).
- AI Copilot message drafting preparation (`AIDraftService`).

---

## 2. Reused Infrastructure
- Reused Spring `ApplicationEventPublisher` for domain event publishing.
- Reused existing JPA + PostgreSQL persistence layer.
- Reused existing `AppUser`, `Customer`, `Tenant`, `RoleType`, `Permission` RBAC security model.
- Reused existing Customer Portal authentication & security context.

---

## 3. Data Model

### Enums
- **`NotificationType`**: `QUOTE_SENT`, `QUOTE_ACCEPTED`, `QUOTE_CHANGE_REQUESTED`, `BOOKING_CONFIRMED`, `BOOKING_CANCELLED`, `PAYMENT_RECEIVED`, `PAYMENT_DUE`, `PAYMENT_FAILED`, `INVOICE_CREATED`, `INVOICE_SENT`, `INVOICE_OVERDUE`, `DELIVERY_SCHEDULED`, `DELIVERY_ASSIGNED`, `DELIVERY_COMPLETED`, `CUSTOMER_REQUEST_CREATED`, `CUSTOMER_REQUEST_UPDATED`, `SYSTEM`.
- **`NotificationChannel`**: `IN_APP`, `EMAIL`, `SMS`.
- **`NotificationStatus`**: `PENDING`, `PROCESSING`, `SENT`, `DELIVERED`, `FAILED`, `READ`, `CANCELLED`.
- **`NotificationPriority`**: `LOW`, `NORMAL`, `HIGH`, `URGENT`.

### Entities
1. **`Notification`**: Stores delivered and pending notifications.
2. **`NotificationTemplate`**: Stores customizable templates per tenant, type, and channel.
3. **`NotificationPreference`**: Stores user/customer channel enablement settings.
4. **`NotificationAudit`**: Stores notification lifecycle audit entries.

---

## 4. Architecture & Providers

```
Domain Event (e.g. PaymentReceivedEvent)
         ↓
NotificationEventListener (@EventListener)
         ↓
NotificationService (Preference check, Idempotency check, Template render)
         ↓
NotificationChannelProvider
 ├── InAppNotificationProviderImpl  → In-App Status: SENT / READ
 ├── MockEmailNotificationProvider   → Mock Console/Log Output
 └── MockSmsNotificationProvider     → Mock Console/Log Output
```

---

## 5. REST APIs

### Internal Staff Endpoints
- `GET /api/notifications`: Paginated & filtered notifications list.
- `GET /api/notifications/unread-count`: Unread notifications count.
- `GET /api/notifications/{id}`: Detail view of a notification.
- `PATCH /api/notifications/{id}/read`: Mark notification as read.
- `PATCH /api/notifications/read-all`: Mark all notifications as read.
- `POST /api/notifications/{id}/retry`: Manually retry failed notification.
- `GET /api/notification-preferences`: Retrieve staff notification preferences.
- `PUT /api/notification-preferences`: Update staff notification preferences.
- `GET /api/notification-templates`: Retrieve notification templates list.
- `POST /api/notification-templates`: Create new template.
- `GET /api/notification-templates/{id}`: Retrieve template details.
- `PUT /api/notification-templates/{id}`: Update template.
- `POST /api/notification-templates/{id}/preview`: Live render template with sample data.

### Customer Portal Endpoints
- `GET /api/portal/notifications`: Customer-scoped notifications list.
- `GET /api/portal/notifications/unread-count`: Customer unread count.
- `PATCH /api/portal/notifications/{id}/read`: Mark customer notification as read.
- `PATCH /api/portal/notifications/read-all`: Mark all customer notifications as read.
- `GET /api/portal/notification-preferences`: Retrieve customer notification preferences.
- `PUT /api/portal/notification-preferences`: Update customer notification preferences.

---

## 6. Security, RBAC & Tenant Isolation
- **Tenant Isolation**: Every database query filters by `tenantId`.
- **Customer Security**: Customer Portal APIs enforce `user.customerId == notification.recipientCustomerId`. Cross-customer access attempts return `403 Forbidden`.
- **RBAC**:
  - `OWNER` / `ADMIN`: Full access to templates, preferences, and notifications.
  - `SALES` / `FINANCE` / `WAREHOUSE`: View operational notifications and preferences.
  - `CUSTOMER`: Access strictly limited to own notifications and own preferences. Cannot manage internal templates.

---

## 7. AI Copilot Preparation
- `AIDraftService` interface provides `draftMessage(DraftRequest request)` functionality.
- Human-in-the-loop requirement: AI drafts messages for review before sending.

---

## 8. Verification Results
- All unit and integration tests (`NotificationServiceTest`, `CustomerPortalNotificationTest`) pass cleanly.
- Full backend suite (`mvn test`) passed 100%.
- Angular frontend build (`npm run build`) completed with zero compilation errors.
