# Day 38: Flyway Database Migration & Production-Safe Schema Management

## 1. Existing Database Architecture
RentFlow AI was designed with a multi-tenant PostgreSQL target architecture (containerized via Docker Compose using `postgres:15-alpine`, database `rentflow_prod`, user `rentflow_user`), while local rapid development uses an in-memory H2 database (`jdbc:h2:mem:rentflow;MODE=PostgreSQL`).
Prior to Day 38:
- Database schema was generated dynamically by Hibernate (`ddl-auto=update`).
- The domain model comprised 114 database tables (112 JPA entities + 2 join tables: `app_user_roles`, `ai_conversation_tags`).
- Core business tables include `tenants`, `app_users`, `customers`, `products`, `quotes`, `booking`, `invoices`, `payments`, `warehouse_stock`, and `rental_requests`.
- As confirmed by database inspection, host PostgreSQL had 0 RentFlow connections and role `rentflow_user` was not yet provisioned. Production baselining on live host was safely blocked to prevent false baselining.

## 2. Flyway Version and Dependencies
- **Flyway Version**: `9.22.3` (managed by `spring-boot-starter-parent` 3.2.4).
- **Dependency in `backend/pom.xml`**:
  ```xml
  <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
  </dependency>
  ```
- **Driver**: `org.postgresql:postgresql:42.6.2` (managed dependency).
- *Note*: Flyway 9.x includes PostgreSQL support natively inside `flyway-core`. The separate `flyway-database-postgresql` artifact only exists in Flyway 10+ and is neither needed nor compatible with 9.22.3.

## 3. Migration Directory Structure
Versioned migrations are placed in the standard classpath location:
`backend/src/main/resources/db/migration/`

```text
backend/src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__security_and_auth_hardening.sql
├── V3__payment_idempotency_constraints.sql
├── V4__booking_checkout_idempotency.sql
└── V5__financial_precision_and_inventory_constraints.sql
```

## 4. Selected Baseline Strategy
**Option A: Versioned Initial Schema** was selected.
- `V1__initial_schema.sql` captures the baseline schema of all 114 tables as of Day 30.
- Subsequent migrations (`V2` through `V5`) cleanly isolate each phase of schema evolution from Days 31 to 37.
- For new databases: Flyway runs `V1` -> `V2` -> `V3` -> `V4` -> `V5` sequentially.
- For existing databases: Flyway baselines the schema at version `1` (which represents the Day 30 state), then applies pending migrations `V2` through `V5` without dropping or truncating tables.

## 5. Baseline Version
- **Baseline Version**: `1`
- **Baseline Description**: `Initial Baseline`
- **Configuration Rule**: `spring.flyway.baseline-on-migrate=false` in production (`application-prod.properties`). Automatic baselining is disabled to avoid accidental unverified baselines against an uninspected database. Baselining is strictly performed via controlled operations.

## 6. Fresh Database Installation
For new production environments or blank databases:
1. An empty PostgreSQL database is provisioned with appropriate user permissions:
   ```sql
   CREATE DATABASE rentflow;
   CREATE USER rentflow_user WITH ENCRYPTED PASSWORD 'StrongSecretPassword!';
   GRANT ALL PRIVILEGES ON DATABASE rentflow TO rentflow_user;
   ```
2. Application starts with `spring.profiles.active=prod`.
3. Flyway executes `V1` through `V5` in order.
4. Schema history is logged in `flyway_schema_history`.
5. Hibernate runs with `ddl-auto=validate`, confirms 0 schema drift, and the application becomes ready.

## 7. Existing Database Onboarding
For environments with existing Day 30 data:
1. **Pre-flight Audit**: Verify all 114 tables exist and match expected definitions.
2. **Backup**: Execute `pg_dump` physical backup.
3. **Data Pre-check**: Verify uniqueness for pending constraints (`payments.transaction_reference`, `rental_requests.idempotency_key`, etc.).
4. **Controlled Baseline**: Execute Flyway baseline at version 1:
   ```bash
   flyway -url=jdbc:postgresql://localhost:5432/rentflow \
          -user=rentflow_user -password=StrongSecretPassword! \
          -baselineVersion=1 \
          -baselineDescription="Initial Baseline" \
          baseline
   ```
5. **Migrate**: Run `flyway migrate` or start Spring Boot. Migrations `V2` through `V5` are applied.
6. **Data Integrity Guarantee**: 100% of existing rows across all tables are preserved.

## 8. Schema-Drift Report
Inspection comparing JPA `@Entity` definitions against the database identified:
- **Table Naming Drift**: `@Entity class BookingItem` maps to table `booking_item` (singular), while `IntegrationOutbox` maps to `integration_outbox`. All 114 tables were mapped to exact Hibernate naming standards in `V1`.
- **Nullable vs Non-null Drift**: Idempotency columns in `rental_requests`, `quotes`, and `crm_leads` were added dynamically in Days 34–35. `V4` safely ensures column presence via `ADD COLUMN IF NOT EXISTS` before adding unique constraints.
- **Orphaned Migration File**: An orphaned `V35__idempotency_constraints.sql` script with incorrect table references was discovered in `db/migration`. It was deleted and replaced by sequential, properly tested `V4` and `V5` scripts.

## 9. Hibernate Validation Configuration
In `application-prod.properties`:
```properties
spring.jpa.hibernate.ddl-auto=${HIBERNATE_DDL_AUTO:validate}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false
```
In development (`application.properties`):
```properties
spring.jpa.hibernate.ddl-auto=update
```
This guarantees that production environments immediately fail to boot if any JPA entity deviates from the Flyway-applied schema.

## 10. Payment Constraints
Introduced in `V3__payment_idempotency_constraints.sql`:
- **Unique Constraint**: `uq_payment_tenant_transaction_reference` on `(tenant_id, transaction_reference)` prevents duplicate payment processing for the same tenant.
- **Index**: `idx_payment_tenant_status` on `(tenant_id, status)` for fast transaction reconciliation.
- **Pre-check Query**:
  ```sql
  SELECT tenant_id, transaction_reference, COUNT(*)
  FROM payments
  WHERE transaction_reference IS NOT NULL
  GROUP BY tenant_id, transaction_reference
  HAVING COUNT(*) > 1;
  ```

## 11. Booking Constraints
Introduced in `V4__booking_checkout_idempotency.sql`:
- **Rental Request Idempotency**: `uq_rental_req_tenant_idempotency` on `(tenant_id, idempotency_key)`.
- **CRM Lead Idempotency**: `uq_crm_lead_tenant_rental_req` on `(tenant_id, rental_request_id)`.
- **Quote-to-Booking Uniqueness**: `uq_booking_tenant_quote` on `(tenant_id, quote_id)` preventing duplicate booking conversions from the same quote.
- **Quote Idempotency Index**: `idx_quotes_tenant_idempotency` on `(tenant_id, idempotency_key)`.

## 12. Financial Column Changes
Reconciled in `V1` and `V5`:
- All currency and monetary fields (`subtotal`, `tax_amount`, `total_amount`, `deposit_amount`, `balance_due`, `rental_price`, `replacement_cost`) use `NUMERIC(19, 4)` to eliminate floating-point rounding errors.
- Default tax rates and fee percentages use `NUMERIC(7, 4)`.

## 13. Inventory Constraints
Introduced in `V5__financial_precision_and_inventory_constraints.sql`:
- **Unique Warehouse Stock**: `uk_wh_stock_product_warehouse` on `(product_id, warehouse_id)`.
- **Non-Negative Quantity Check**: `chk_products_quantity_owned_nonneg` enforcing `quantity_owned >= 0`.
- **Reservation Date Range Index**: `idx_inv_res_product_dates` on `inventory_reservations(product_id, start_date_time, end_date_time)`.

## 14. Index Strategy
All multi-tenant lookup pathways include composite indexes prefixed by `tenant_id`:
- Security: `idx_refresh_tokens_tenant_token` on `refresh_tokens(tenant_id, token_hash)`.
- Audit: `idx_security_audit_tenant_time` on `security_audit_logs(tenant_id, timestamp)`.
- CRM: `idx_crm_leads_tenant_status` on `crm_leads(tenant_id, status)`.
- Warehouse: `idx_wh_stock_tenant_product` on `warehouse_stock(tenant_id, product_id)`.

## 15. Data-Preservation Checks
The `FlywayExistingDatabaseTest` integration test explicitly verifies:
- Baseline is applied without table drops.
- Pre-existing rows in `tenants`, `customers`, `products`, `quotes`, `booking`, `payments`, `invoices` remain intact after `V2..V5` migrations.
- Data integrity checks pass with 100% data preservation.

## 16. Migration Testing
Integration test suite in `com.rentflow.migration`:
1. `FlywayFreshDatabaseTest`: Tests empty database execution of `V1` to `V5`, verifies table count, indexes, and constraint enforcement.
2. `FlywayExistingDatabaseTest`: Tests baselining existing database at `V1`, incremental application of `V2` to `V5`, and data preservation.
3. `FlywayMigrationValidationTest`: Tests repeat startup idempotency, checksum mismatch detection, duplicate pre-check verification, and failure abort.
4. `FlywaySchemaValidationTest`: Tests full Spring Boot boot with Flyway applied and `spring.jpa.hibernate.ddl-auto=validate`.

## 17. Migration Failure Recovery
If a migration fails mid-way:
1. **Diagnosis**: Inspect logs for the failing SQL statement and error code.
2. **Transaction Rollback**: PostgreSQL DDL is transactional (with few exceptions). If a statement errors, the transaction rolls back.
3. **History Fix**: If the failed migration is recorded with `success = false` in `flyway_schema_history`, resolve the root cause in the database and run `flyway repair`.
4. **Never edit an applied migration**: Always roll forward with a subsequent versioned patch migration.

## 18. Backup Prerequisites
Prior to executing migrations in staging or production:
- Perform full physical backup:
  ```bash
  pg_dump -h localhost -p 5432 -U rentflow_user -F c -b -v -f "/var/backups/rentflow_pre_migration_$(date +%Y%m%d_%H%M%S).dump" rentflow_prod
  ```
- Store backup in immutable offsite storage.
- Verify backup file size and checksum.

## 19. Rollback Limitations
- Flyway Community Edition does not support automated down-migrations (`U` scripts).
- Reverting a migration requires applying a new forward migration (e.g., `V6__drop_deprecated_constraint.sql`) or restoring the database from the pre-migration backup.
- Forward-only migrations are enforced for auditability.

## 20. Deployment Considerations
- **Blue/Green & Rolling Deployments**: Migrations must adhere to the **Expand and Contract pattern**:
  - Step 1: Add new column as nullable or with default.
  - Step 2: Deploy new application code that writes to both columns.
  - Step 3: Backfill data.
  - Step 4: Deploy code that reads new column.
  - Step 5: Drop or deprecate old column in a later release.
- **Lock Timeouts**: For high-traffic tables, apply DDL with a short `lock_timeout` to prevent blocking production transactions.

## 21. Remaining Migration Risks
1. **Host PostgreSQL Readiness**: Host PostgreSQL server currently lacks role `rentflow_user` and database `rentflow_prod`. Database provisioning scripts must be executed prior to production startup.
2. **Large Table Index Creation**: In high-volume production databases, future index additions on tables with millions of rows should be executed using `CREATE INDEX CONCURRENTLY` outside of Flyway transaction blocks or during maintenance windows.
3. **Strict Validation Drift**: Any future change to JPA entity annotations without a matching Flyway SQL script will prevent Spring Boot from booting in production. This strict behavior is intended to protect production data integrity.
